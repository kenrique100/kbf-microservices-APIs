package com.akentech.kbf.income.kafka.consumer;

import com.akentech.kbf.income.exception.IncomeNotFoundException;
import com.akentech.kbf.income.kafka.producer.TransactionProducer;
import com.akentech.kbf.income.repository.IncomeRepository;
import com.akentech.kbf.income.repository.ProcessedDataIncomeRepository;
import com.akentech.kbf.income.utils.ValidationUtils;
import com.akentech.shared.models.Income;
import com.akentech.shared.models.ProcessedDataIncome;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncomeConsumer {
    private static final String SYSTEM_USER = "SYSTEM";

    private final IncomeRepository incomeRepository;
    private final ProcessedDataIncomeRepository processedDataRepo;
    private final TransactionProducer transactionProducer;

    @KafkaListener(topics = "income-processing-topic", groupId = "income-group")
    @Retryable(
            value = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void processIncome(Income income) {
        log.info("Processing income with ID: {}", income.getId());

        incomeRepository.findById(income.getId())
                .switchIfEmpty(Mono.error(new IncomeNotFoundException("Income not found with ID: " + income.getId())))
                .flatMap(existingIncome -> {
                    if (!Income.ProcessingStatus.PENDING.name().equals(existingIncome.getStatus())) {
                        log.warn("Income with ID {} is already processed with status: {}",
                                existingIncome.getId(), existingIncome.getStatus());
                        return Mono.error(new IllegalStateException("Income already processed"));
                    }
                    return processValidIncome(existingIncome);
                })
                .subscribe(
                        result -> log.info("Successfully processed income ID: {}", result.getId()),
                        error -> log.error("Failed to process income ID {}: {}", income.getId(), error.getMessage())
                );
    }

    private Mono<Income> processValidIncome(Income income) {
        income.setStatus(Income.ProcessingStatus.PROCESSING.name());
        calculateDueBalance(income);

        return incomeRepository.save(income)
                .flatMap(this::validateAndProcessIncome)
                .onErrorResume(e -> handleProcessingError(income, e));
    }

    private Mono<Income> validateAndProcessIncome(Income income) {
        return Mono.fromCallable(() -> {
                    ValidationUtils.validateIncome(income);
                    return income;
                })
                .flatMap(validIncome -> {
                    validIncome.setStatus(Income.ProcessingStatus.SUCCESS.name());
                    return incomeRepository.save(validIncome)
                            .flatMap(savedIncome -> createProcessedData(savedIncome)
                                    .flatMap(processedData -> transactionProducer.sendTransaction(income))
                                    .thenReturn(income));
                });
    }

    private void calculateDueBalance(Income income) {
        if (income.getExpectedAmount() != null && income.getAmountReceived() != null) {
            income.setDueBalance(income.getExpectedAmount().subtract(income.getAmountReceived()));
        } else {
            income.setDueBalance(BigDecimal.ZERO);
            log.warn("Missing expectedAmount or amountReceived for income {}, setting dueBalance to 0", income.getId());
        }
    }

    private Mono<ProcessedDataIncome> createProcessedData(Income income) {
        ProcessedDataIncome processedData = ProcessedDataIncome.builder()
                .incomeId(income.getId())
                .processedAmount(income.getAmountReceived())
                .status(income.getStatus())
                .processedAt(LocalDate.now())
                .processedBy(SYSTEM_USER)
                .build();

        return processedDataRepo.save(processedData)
                .doOnSuccess(p -> log.debug("Created processed data record for income ID: {}", income.getId()));
    }

    private Mono<Income> handleProcessingError(Income income, Throwable error) {
        income.setStatus(Income.ProcessingStatus.FAILED.name());
        income.setErrorMessage(error.getMessage());

        return incomeRepository.save(income)
                .doOnSuccess(i -> log.error("Marked income ID {} as failed due to: {}", i.getId(), error.getMessage()));
    }
}