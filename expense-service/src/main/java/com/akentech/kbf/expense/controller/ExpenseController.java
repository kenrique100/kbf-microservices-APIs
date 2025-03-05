package com.akentech.kbf.expense.controller;

import com.akentech.kbf.expense.model.Expense;
import com.akentech.kbf.expense.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public Flux<Expense> getAllExpenses() {
       /* try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
        return expenseService.getAllExpenses();
    }

    @GetMapping("/{id}")
    public Mono<Expense> getExpenseById(@PathVariable String id) {
        return expenseService.getExpenseById(id)
                .doOnError(error -> log.error("Error fetching expense with ID {}: {}", id, error.getMessage()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Expense> createExpense(@RequestBody @Valid Expense expense) {
        return expenseService.createExpense(expense)
                .doOnError(error -> log.error("Error creating expense: {}", error.getMessage()));
    }

    @PutMapping("/{id}")
    public Mono<Expense> updateExpense(@PathVariable String id, @RequestBody Expense expense) {
        return expenseService.updateExpense(id, expense)
                .doOnError(error -> log.error("Error updating expense with ID {}: {}", id, error.getMessage()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteExpense(@PathVariable String id) {
        return expenseService.deleteExpense(id)
                .doOnError(error -> log.error("Error deleting expense with ID {}: {}", id, error.getMessage()));
    }
}