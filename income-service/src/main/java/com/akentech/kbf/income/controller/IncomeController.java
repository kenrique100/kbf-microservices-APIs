package com.akentech.kbf.income.controller;

import com.akentech.kbf.income.exception.IncomeNotFoundException;
import com.akentech.kbf.income.utils.ValidationUtils;
import com.akentech.shared.models.Income;
import com.akentech.kbf.income.service.IncomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
@Slf4j
public class IncomeController {

    private final IncomeService incomeService;

    @GetMapping
    public Flux<Income> getAllIncomes() {
        return incomeService.getAllIncomes();
    }

    @GetMapping("/{id}")
    public Mono<Income> getIncomeById(@PathVariable Long id) {
        ValidationUtils.validateIncomeId(id);
        return incomeService.getIncomeById(id)
                .switchIfEmpty(Mono.error(() -> new IncomeNotFoundException("Income not found with ID: " + id)))
                .doOnError(error -> log.error("Error fetching income with ID {}: {}", id, error.getMessage()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Income> createIncome(@RequestBody @Valid Income income) {
        ValidationUtils.validateIncome(income);
        return incomeService.createIncome(income)
                .doOnError(error -> log.error("Error creating income: {}", error.getMessage()));
    }

    @PutMapping("/{id}")
    public Mono<Income> updateIncome(@PathVariable Long id, @RequestBody @Valid Income income) {
        ValidationUtils.validateIncomeId(id);
        ValidationUtils.validateIncome(income);
        return incomeService.updateIncome(id, income)
                .switchIfEmpty(Mono.error(() -> new IncomeNotFoundException("Income not found with ID: " + id)))
                .doOnError(error -> log.error("Error updating income with ID {}: {}", id, error.getMessage()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteIncome(@PathVariable Long id) {
        ValidationUtils.validateIncomeId(id);
        return incomeService.deleteIncome(id)
                .switchIfEmpty(Mono.error(() -> new IncomeNotFoundException("Income not found with ID: " + id)))
                .doOnError(error -> log.error("Error deleting income with ID {}: {}", id, error.getMessage()));
    }
}