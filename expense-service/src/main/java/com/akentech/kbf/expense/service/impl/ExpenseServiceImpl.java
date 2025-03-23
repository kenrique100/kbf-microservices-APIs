package com.akentech.kbf.expense.service.impl;

import com.akentech.kbf.expense.repository.ExpenseRepository;
import com.akentech.kbf.expense.service.ExpenseService;
import com.akentech.kbf.expense.utils.ValidationUtils;
import com.akentech.shared.models.Expense;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Flux<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    @Override
    public Mono<Expense> getExpenseById(Long id) {
        return expenseRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found")));
    }

    @Override
    public Mono<Expense> createExpense(Expense expense) {
        ValidationUtils.validateExpense(expense);
        expense.calculateDueBalance();

        return expenseRepository.save(expense)
                .doOnSuccess(savedExpense -> kafkaTemplate.send("expense-topic", savedExpense.getId().toString(), savedExpense));
    }

    @Override
    public Mono<Expense> updateExpense(Long id, Expense expense) {
        ValidationUtils.validateExpense(expense); // Validate expense before updating

        return expenseRepository.findById(id)
                .flatMap(existingExpense -> {
                    existingExpense.setReason(expense.getReason());
                    existingExpense.setExpenseDate(expense.getExpenseDate());
                    existingExpense.setQtyPurchased(expense.getQtyPurchased());
                    existingExpense.setAmountPaid(expense.getAmountPaid());
                    existingExpense.setExpectedAmount(expense.getExpectedAmount());
                    existingExpense.calculateDueBalance();
                    existingExpense.setReceipt(expense.getReceipt());
                    existingExpense.setCreatedBy(expense.getCreatedBy());

                    return expenseRepository.save(existingExpense);
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found")));
    }

    @Override
    public Mono<Void> deleteExpense(Long id) {
        return expenseRepository.existsById(id)
                .flatMap(exists -> exists
                        ? expenseRepository.deleteById(id)
                        : Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found")));
    }
}