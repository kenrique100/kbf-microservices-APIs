package com.akentech.kbf.income.service.impl;

import com.akentech.kbf.income.repository.IncomeRepository;
import com.akentech.kbf.income.repository.ProcessedDataIncomeRepository;
import com.akentech.kbf.income.service.IncomeService;
import com.akentech.kbf.income.utils.ValidationUtils;
import com.akentech.shared.models.Income;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {
    private final IncomeRepository incomeRepository;
    private final ProcessedDataIncomeRepository processedDataRepo;
    private final TransactionalOperator transactionalOperator;

    @Override
    public Flux<Income> getAllIncomes() {
        return incomeRepository.findAll();
    }

    @Override
    public Mono<Income> getIncomeById(Long id) {
        return incomeRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found")));
    }

    @Override
    public Mono<Income> createIncome(Income income) {
        ValidationUtils.validateIncome(income);
        income.calculateDueBalance();
        income.setStatus(Income.ProcessingStatus.PENDING.toString());
        return incomeRepository.save(income);
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
                    return incomeRepository.save(existingIncome);
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found")));
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
                            .then(processedDataRepo.deleteByIncomeId(id))
                            .onErrorResume(e -> {
                                log.error("Failed to delete income with ID {}: {}", id, e.getMessage());
                                return Mono.error(new ResponseStatusException(
                                        HttpStatus.INTERNAL_SERVER_ERROR,
                                        "Failed to delete income"));
                            });
                });
    }
}