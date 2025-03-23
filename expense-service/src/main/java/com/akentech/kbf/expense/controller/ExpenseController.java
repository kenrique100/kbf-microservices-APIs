package com.akentech.kbf.expense.controller;

import com.akentech.kbf.expense.service.ExpenseService;
import com.akentech.shared.models.Expense;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public Flux<Expense> getAllExpenses() {
        return expenseService.getAllExpenses();
    }

    @GetMapping("/{id}")
    public Mono<Expense> getExpenseById(@PathVariable Long id) {
        return expenseService.getExpenseById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Expense> createExpense(@RequestBody @Valid Expense expense) {
        return expenseService.createExpense(expense)
                .doOnError(error -> log.error("Error creating expense: {}", error.getMessage()));
    }

    @PutMapping("/{id}")
    public Mono<Expense> updateExpense(@PathVariable Long id, @RequestBody @Valid Expense expense) {
        return expenseService.updateExpense(id, expense);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteExpense(@PathVariable Long id) {
        return expenseService.deleteExpense(id);
    }
}