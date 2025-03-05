package com.akentech.kbf.expense.service.impl;

import com.akentech.kbf.expense.model.Expense;
import com.akentech.kbf.expense.repository.ExpenseRepository;
import com.akentech.kbf.expense.service.ExpenseService;
import com.akentech.kbf.expense.utils.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.bson.types.ObjectId;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Override
    public Flux<Expense> getAllExpenses() {
        LoggingUtil.logInfo("Fetching all expenses");
        return expenseRepository.findAll();
    }

    @Override
    public Mono<Expense> getExpenseById(String id) {
        if (id == null || id.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID cannot be null or empty"));
        }

        // Validate ObjectId format
        if (!ObjectId.isValid(id)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format"));
        }

        LoggingUtil.logInfo("Fetching expense by ID: " + id);

        return expenseRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found with id: " + id)))
                .map(expense -> {
                    expense.calculateDueBalance(); // Ensure dueBalance is set
                    return expense;
                })
                .onErrorResume(e -> {
                    LoggingUtil.logError("Error fetching expense by ID: " + id + ", Error: " + e.getMessage());
                    return Mono.error(e);
                });
    }

    @Override
    public Mono<Expense> createExpense(Expense expense) {
        LoggingUtil.logInfo("Creating new expense: " + expense.getReason());
        expense.calculateDueBalance();
        return expenseRepository.save(expense);
    }

    @Override
    public Mono<Expense> updateExpense(String id, Expense expense) {
        if (id == null || id.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID cannot be null or empty"));
        }

        // Validate ObjectId format
        if (!ObjectId.isValid(id)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format"));
        }

        // Validate expense fields
        if (expense.getAmountPaid() == null || expense.getAmountPaid().compareTo(BigDecimal.ZERO) < 0) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount paid must be positive"));
        }

        LoggingUtil.logInfo("Updating expense with ID: " + id);

        return expenseRepository.findById(id)
                .flatMap(existingExpense -> {
                    existingExpense.setReason(expense.getReason());
                    existingExpense.setExpenseDate(expense.getExpenseDate());
                    existingExpense.setQtyPurchased(expense.getQtyPurchased());
                    existingExpense.setAmountPaid(expense.getAmountPaid());
                    existingExpense.setExpectedAmount(expense.getExpectedAmount());
                    existingExpense.calculateDueBalance(); // Recalculate due balance
                    existingExpense.setReceipt(expense.getReceipt());
                    existingExpense.setCreatedBy(expense.getCreatedBy());
                    return expenseRepository.save(existingExpense);
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found with id: " + id)));
    }

    @Override
    public Mono<Void> deleteExpense(String id) {
        if (!ObjectId.isValid(id)) { // Validate MongoDB ObjectId format
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format"));
        }

        return expenseRepository.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));
                    }
                    return expenseRepository.deleteById(id);
                });
    }
}