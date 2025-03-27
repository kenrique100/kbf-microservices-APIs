package com.akentech.kbf.income.service.impl;

import com.akentech.kbf.income.repository.IncomeRepository;
import com.akentech.kbf.income.repository.ProcessedDataIncomeRepository;
import com.akentech.kbf.income.service.IncomeService;
import com.akentech.shared.models.Income;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {
    private final IncomeRepository incomeRepository;
    private final ProcessedDataIncomeRepository processedDataRepo;

    @Override
    public Flux<Income> getAllIncomes() {
        return incomeRepository.findAll()
                .doOnError(error -> log.error("Error fetching all incomes: {}", error.getMessage()));
    }

    @Override
    public Flux<Income> getPendingIncomes() {
        return incomeRepository.findByStatus(Income.ProcessingStatus.PENDING.name())
                .doOnError(error -> log.error("Error fetching pending incomes: {}", error.getMessage()));
    }

    @Override
    public Mono<Boolean> checkForDuplicate(Income income) {
        return incomeRepository.existsByReasonAndIncomeDateAndAmountReceived(
                income.getReason(),
                income.getIncomeDate(),
                income.getAmountReceived()
        );
    }

    @Override
    public Flux<Income> getFailedIncomes() {
        return incomeRepository.findByStatus(Income.ProcessingStatus.FAILED.name())
                .doOnError(error -> log.error("Error fetching failed incomes: {}", error.getMessage()));
    }

    @Override
    public Mono<Income> getIncomeById(Long id) {
        return incomeRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Income not found with ID: " + id
                )))
                .doOnError(error -> log.error("Error fetching income with ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Income> createIncome(Income income) {
        income.calculateDueBalance();
        income.setStatus(Income.ProcessingStatus.PENDING.name());
        income.setCreatedDate(LocalDateTime.now());

        return incomeRepository.save(income)
                .doOnSuccess(savedIncome -> log.info("Income created with ID: {}", savedIncome.getId()))
                .doOnError(error -> log.error("Error creating income: {}", error.getMessage()));
    }

    @Override
    public Mono<Income> updateIncome(Long id, Income income) {
        return incomeRepository.findById(id)
                .flatMap(existingIncome -> {
                    existingIncome.setReason(income.getReason());
                    existingIncome.setIncomeDate(income.getIncomeDate());
                    existingIncome.setQuantity(income.getQuantity());
                    existingIncome.setAmountReceived(income.getAmountReceived());
                    existingIncome.setExpectedAmount(income.getExpectedAmount());
                    existingIncome.calculateDueBalance();
                    existingIncome.setReceipt(income.getReceipt());
                    existingIncome.setCreatedBy(income.getCreatedBy());
                    existingIncome.setStatus(Income.ProcessingStatus.PENDING.name());
                    existingIncome.setErrorMessage(null);
                    return incomeRepository.save(existingIncome);
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Income not found with ID: " + id
                )))
                .doOnError(error -> log.error("Error updating income with ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Void> deleteIncome(Long id) {
        return incomeRepository.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Income not found with ID: " + id));
                    }
                    return incomeRepository.deleteById(id)
                            .then(processedDataRepo.deleteByIncomeId(id));
                })
                .doOnError(error -> log.error("Error deleting income with ID {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Income> retryFailedIncome(Long id) {
        return incomeRepository.findById(id)
                .flatMap(income -> {
                    if (!Income.ProcessingStatus.FAILED.name().equals(income.getStatus())) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Income is not in FAILED status"));
                    }
                    income.setStatus(Income.ProcessingStatus.PENDING.name());
                    income.setErrorMessage(null);
                    return incomeRepository.save(income);
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Income not found with ID: " + id
                )))
                .doOnError(error -> log.error("Error retrying failed income with ID {}: {}", id, error.getMessage()));
    }
}