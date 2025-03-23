package com.akentech.kbf.expense.service;

import com.akentech.shared.models.Expense;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ExpenseService {
    Flux<Expense> getAllExpenses();
    Mono<Expense> getExpenseById(Long id);
    Mono<Expense> createExpense(Expense expense);
    Mono<Expense> updateExpense(Long id, Expense expense);
    Mono<Void> deleteExpense(Long id);
}