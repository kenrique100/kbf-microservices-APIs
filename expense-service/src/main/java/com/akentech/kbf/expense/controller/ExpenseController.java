package com.akentech.kbf.expense.controller;

import com.akentech.shared.models.Expense;
import com.akentech.kbf.expense.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * Fetches all expenses.
     *
     * @return A Flux of Expense objects representing all expenses.
     */
    @GetMapping
    public Flux<Expense> getAllExpenses() {
        return expenseService.getAllExpenses();
    }

    /**
     * Fetches an expense by its ID.
     *
     * @param id The ID of the expense to fetch.
     * @return A Mono of Expense if found, otherwise an error.
     */
    @GetMapping("/{id}")
    public Mono<Expense> getExpenseById(@PathVariable String id) {
        return expenseService.getExpenseById(id);
    }

    /**
     * Creates a new expense.
     *
     * @param expense The expense object to create.
     * @return A Mono of the created Expense.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Expense> createExpense(@RequestBody @Valid Expense expense) {
        return expenseService.createExpense(expense);
    }

    /**
     * Updates an existing expense by its ID.
     *
     * @param id The ID of the expense to update.
     * @param expense The updated expense object.
     * @return A Mono of the updated Expense.
     */
    @PutMapping("/{id}")
    public Mono<Expense> updateExpense(@PathVariable String id, @RequestBody Expense expense) {
        return expenseService.updateExpense(id, expense);
    }

    /**
     * Deletes an expense by its ID.
     *
     * @param id The ID of the expense to delete.
     * @return A Mono of Void.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteExpense(@PathVariable String id) {
        return expenseService.deleteExpense(id);
    }
}