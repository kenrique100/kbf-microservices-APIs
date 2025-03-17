package com.akentech.kbf.income.service.impl;

import com.akentech.kbf.income.repository.IncomeRepository;
import com.akentech.kbf.income.service.IncomeService;
import com.akentech.kbf.kafka.utils.LoggingUtil;
import com.akentech.shared.models.Income;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.bson.types.ObjectId;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final KafkaTemplate<String, Object> KafkaTemplate; // Kafka template for publishing events

    /**
     * Fetches all income records from the database.
     *
     * @return A Flux of Income objects representing all incomes.
     */
    @Override
    public Flux<Income> getAllIncomes() {
        LoggingUtil.logInfo("Fetching all incomes");
        return incomeRepository.findAll();
    }

    /**
     * Fetches a single income record by its ID.
     *
     * @param id The ID of the income record to fetch.
     * @return A Mono of Income if found, or an error if the ID is invalid or the record is not found.
     */
    @Override
    public Mono<Income> getIncomeById(String id) {
        if (id == null || id.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID cannot be null or empty"));
        }

        // Validate that the ID is in a valid MongoDB ObjectId format
        if (!ObjectId.isValid(id)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format"));
        }

        LoggingUtil.logInfo("Fetching income by ID: " + id);

        return incomeRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found with id: " + id)))
                .map(income -> {
                    income.calculateDueBalance();
                    return income;
                })
                .onErrorResume(e -> {
                    LoggingUtil.logError("Error fetching income by ID: " + id + ", Error: " + e.getMessage());
                    return Mono.error(e);
                });
    }

    /**
     * Creates a new income record and sets the creation timestamp.
     *
     * @param income The income object to be created.
     * @return A Mono of the saved Income object with the creation timestamp.
     */
    @Override
    public Mono<Income> createIncome(Income income) {
        LoggingUtil.logInfo("Creating new income: " + income.getReason());
        income.calculateDueBalance();
        //income.setCreatedAt(LocalDateTime.now());
        return incomeRepository.save(income)
                .doOnSuccess(savedIncome -> {
                    // Publish the income data to Kafka
                    KafkaTemplate.send("income-topic", savedIncome);
                    LoggingUtil.logInfo("Income event published: " + savedIncome.getId());
                });
    }

    /**
     * Updates an existing income record by its ID.
     *
     * @param id The ID of the income record to update.
     * @param income The updated income object.
     * @return A Mono of the updated Income object, or an error if the ID is invalid or the record is not found.
     */
    @Override
    public Mono<Income> updateIncome(String id, Income income) {
        if (id == null || id.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID cannot be null or empty"));
        }

        // Validate that the ID is in a valid MongoDB ObjectId format
        if (!ObjectId.isValid(id)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format"));
        }

        // Validate that the amount received is positive
        if (income.getAmountReceived() == null || income.getAmountReceived().compareTo(BigDecimal.ZERO) < 0) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount received must be positive"));
        }

        LoggingUtil.logInfo("Updating income with ID: " + id);

        return incomeRepository.findById(id)
                .flatMap(existingIncome -> {
                    // Update the existing income record with new values
                    existingIncome.setReason(income.getReason());
                    existingIncome.setIncomeDate(income.getIncomeDate());
                    existingIncome.setQuantity(income.getQuantity());
                    existingIncome.setAmountReceived(income.getAmountReceived());
                    existingIncome.setExpectedAmount(income.getExpectedAmount());
                    existingIncome.calculateDueBalance(); // Recalculate due balance
                    existingIncome.setReceipt(income.getReceipt());
                    existingIncome.setCreatedBy(income.getCreatedBy());
                    return incomeRepository.save(existingIncome); // Save the updated record
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found with id: " + id)));
    }

    /**
     * Deletes an income record by its ID.
     *
     * @param id The ID of the income record to delete.
     * @return A Mono of Void, or an error if the ID is invalid or the record is not found.
     */
    @Override
    public Mono<Void> deleteIncome(String id) {
        if (!ObjectId.isValid(id)) { // Validate that the ID is in a valid MongoDB ObjectId format
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format"));
        }

        return incomeRepository.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found"));
                    }
                    return incomeRepository.deleteById(id)
                            .doOnSuccess(unused -> LoggingUtil.logInfo("Income deleted with ID: " + id));
                });
    }
}