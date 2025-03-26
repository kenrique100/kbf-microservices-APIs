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
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncomeConsumer {
    private final IncomeRepository incomeRepository;
    private final ProcessedDataIncomeRepository processedDataRepo;
    private final TransactionalOperator transactionalOperator;

    @KafkaListener(topics = "income-processing-topic", groupId = "income-group")
    public void processIncome(Income income) {
        log.info("Received income for processing: {}", income);

        // Initialize status if null
        if (income.getStatus() == null) {
            income.setStatus(Income.ProcessingStatus.PENDING.name());
        }

        // Wrap in transaction
        transactionalOperator.execute(status ->
                incomeRepository.findByReasonAndIncomeDate(income.getReason(), income.getIncomeDate())
                        .hasElements()
                        .flatMap(exists -> {
                            if (Boolean.TRUE.equals(exists)) {
                                log.warn("Duplicate income found: {}", income);
                                return Mono.error(new DuplicateIncomeException("Duplicate record found"));
                            }
                            return processValidIncome(income);
                        })
                        .onErrorResume(e -> handleFailedIncome(income, e))
        ).subscribe(
                result -> log.info("Successfully processed income with ID: {}", result.getId()),
                error -> log.error("Failed to process income: {}", error.getMessage()),
                () -> log.debug("Income processing completed")
        );
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
                            .status(savedIncome.getStatus())
                            .processedAt(LocalDate.now())
                            .processedBy("SYSTEM")
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
                            .build();

                    return processedDataRepo.save(processedData)
                            .thenReturn(savedIncome);
                });
    }
}