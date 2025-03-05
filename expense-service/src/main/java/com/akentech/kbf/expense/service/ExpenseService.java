package com.akentech.kbf.expense.service;

import com.akentech.kbf.expense.model.Expense;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpenseService {
    Flux<Expense> getAllExpenses();
    Mono<Expense> getExpenseById(String id);
    Mono<Expense> createExpense(Expense expense);
    Mono<Expense> updateExpense(String id, Expense expense);
    Mono<Void> deleteExpense(String id);
}