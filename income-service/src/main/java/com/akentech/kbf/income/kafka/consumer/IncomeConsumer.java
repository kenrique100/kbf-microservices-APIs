package com.akentech.kbf.income.kafka.consumer;

import com.akentech.kbf.income.exception.DuplicateIncomeException;
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
import org.springframework.transaction.reactive.TransactionalOperator;
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
    private final TransactionalOperator transactionalOperator;

    @KafkaListener(topics = "income-processing-topic", groupId = "income-group")
    @Retryable(
            value = {Exception.class},
            backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void processIncome(Income income) {
        log.info("Processing income: {}", income.getId());

        // Calculate due balance before processing
        calculateAndSetDueBalance(income);

        transactionalOperator.execute(status ->
                incomeRepository.findByReasonAndIncomeDate(income.getReason(), income.getIncomeDate())
                        .hasElements()
                        .flatMap(exists -> {
                            if (Boolean.TRUE.equals(exists)) {
                                return Mono.error(new DuplicateIncomeException(
                                        "Duplicate income with reason: " + income.getReason() +
                                                " and date: " + income.getIncomeDate()));
                            }
                            return processValidIncome(income);
                        })
                        .onErrorResume(e -> handleFailedIncome(income, e))
        ).subscribe(
                result -> log.info("Successfully processed income ID: {}", result.getId()),
                error -> log.error("Failed to process income: {}", error.getMessage())
        );
    }

    private void calculateAndSetDueBalance(Income income) {
        if (income.getExpectedAmount() != null && income.getAmountReceived() != null) {
            income.setDueBalance(income.getExpectedAmount().subtract(income.getAmountReceived()));
        } else {
            income.setDueBalance(BigDecimal.ZERO);
            log.warn("Missing expectedAmount or amountReceived for income {}, setting dueBalance to 0", income.getId());
        }
    }

    private Mono<Income> processValidIncome(Income income) {
        return Mono.fromCallable(() -> {
                    ValidationUtils.validateIncome(income);
                    income.setStatus(Income.ProcessingStatus.SUCCESS.name());
                    return income;
                })
                .flatMap(incomeRepository::save)
                .flatMap(savedIncome -> {
                    ProcessedDataIncome processedData = ProcessedDataIncome.builder()
                            .incomeId(savedIncome.getId())
                            .processedAmount(savedIncome.getAmountReceived()) // Store the processed amount
                            .status(savedIncome.getStatus())
                            .processedAt(LocalDate.now())
                            .processedBy(SYSTEM_USER)
                            .build();

                    return processedDataRepo.save(processedData)
                            .thenReturn(savedIncome);
                });
    }

    private Mono<Income> handleFailedIncome(Income income, Throwable error) {
        income.setStatus(Income.ProcessingStatus.FAILED.name());
        income.setErrorMessage(error.getMessage());

        return incomeRepository.save(income)
                .flatMap(savedIncome -> {
                    ProcessedDataIncome processedData = ProcessedDataIncome.builder()
                            .incomeId(savedIncome.getId())
                            .status(savedIncome.getStatus())
                            .errorMessage(savedIncome.getErrorMessage())
                            .processedAt(LocalDate.now())
                            .processedBy(SYSTEM_USER)
                            .build();

                    return processedDataRepo.save(processedData)
                            .thenReturn(savedIncome);
                });
    }
}