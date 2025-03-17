package com.akentech.kbf.expense.service.impl;

import com.akentech.shared.models.Expense;
import com.akentech.kbf.expense.service.ExpenseService;
import com.akentech.kbf.expense.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.bson.types.ObjectId;
import java.math.BigDecimal;

/**
 * Implementation of {@link ExpenseService} that handles CRUD operations and Kafka event publishing.
 */
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Retrieves all expenses from the database.
     *
     * @return a {@link Flux} stream of {@link Expense} objects.
     */
    @Override
    public Flux<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    /**
     * Retrieves an expense by its ID.
     *
     * @param id The ID of the expense to retrieve.
     * @return a {@link Mono} containing the found {@link Expense}, or an error if not found.
     */
    @Override
    public Mono<Expense> getExpenseById(String id) {
        // Validate ID format
        if (id == null || id.isBlank() || !ObjectId.isValid(id)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID"));
        }

        // Fetch expense and calculate due balance
        return expenseRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found with id: " + id)))
                .map(expense -> {
                    expense.calculateDueBalance();  // Ensure calculated fields are updated
                    return expense;
                });
    }

    /**
     * Creates a new expense entry in the database and publishes it to Kafka.
     *
     * @param expense The {@link Expense} object to create.
     * @return a {@link Mono} containing the saved {@link Expense}.
     */
    @Override
    public Mono<Expense> createExpense(Expense expense) {
        expense.calculateDueBalance(); // Calculate due balance before saving

        return expenseRepository.save(expense)
                .flatMap(savedExpense -> {
                    // Ensure ID exists before publishing to Kafka
                    if (savedExpense.getId() != null) {
                        kafkaTemplate.send("expense-topic", savedExpense.getId().toString(), savedExpense);
                    }
                    return Mono.just(savedExpense);
                });
    }

    /**
     * Updates an existing expense in the database.
     *
     * @param id      The ID of the expense to update.
     * @param expense The updated {@link Expense} object.
     * @return a {@link Mono} containing the updated {@link Expense}, or an error if not found.
     */
    @Override
    public Mono<Expense> updateExpense(String id, Expense expense) {
        // Validate ID
        if (id == null || id.isBlank() || !ObjectId.isValid(id)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID"));
        }

        // Validate amount paid (must be non-negative)
        if (expense.getAmountPaid() == null || expense.getAmountPaid().compareTo(BigDecimal.ZERO) < 0) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount paid must be positive"));
        }

        // Find and update the expense
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

    /**
     * Deletes an expense by its ID.
     *
     * @param id The ID of the expense to delete.
     * @return a {@link Mono<Void>} indicating completion, or an error if not found.
     */
    @Override
    public Mono<Void> deleteExpense(String id) {
        // Validate ID
        if (!ObjectId.isValid(id)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID"));
        }

        // Check if the expense exists before attempting deletion
        return expenseRepository.existsById(id)
                .flatMap(exists -> exists
                        ? expenseRepository.deleteById(id)
                        : Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found")));
    }
}
