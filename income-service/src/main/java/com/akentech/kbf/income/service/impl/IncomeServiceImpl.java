package com.akentech.kbf.income.service.impl;

import com.akentech.kbf.income.repository.IncomeRepository;
import com.akentech.kbf.income.service.IncomeService;
import com.akentech.shared.models.Income;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Implementation of the IncomeService interface.
 */
@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Retrieves all income records.
     *
     * @return A Flux of Income objects.
     */
    @Override
    public Flux<Income> getAllIncomes() {
        return incomeRepository.findAll();
    }

    /**
     * Retrieves an income record by ID.
     *
     * @param id The ID of the income record.
     * @return A Mono containing the Income if found.
     */
    @Override
    public Mono<Income> getIncomeById(String id) {
        if (!ObjectId.isValid(id)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format"));
        }

        return incomeRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found")));
    }

    /**
     * Creates a new income record and publishes an event to Kafka.
     *
     * @param income The Income object to be created.
     * @return A Mono containing the saved Income.
     */
    @Override
    public Mono<Income> createIncome(Income income) {
        income.calculateDueBalance();

        return incomeRepository.save(income)
                .map(savedIncome -> {
                    if (savedIncome.getId() != null) {
                        kafkaTemplate.send("income-topic", savedIncome.getId().toString(), savedIncome);
                    }
                    return savedIncome;
                });
    }

    /**
     * Updates an existing income record.
     *
     * @param id The ID of the income record to update.
     * @param income The updated Income object.
     * @return A Mono containing the updated Income.
     */
    @Override
    public Mono<Income> updateIncome(String id, Income income) {
        if (!ObjectId.isValid(id)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format"));
        }

        return incomeRepository.findById(id)
                .flatMap(existingIncome -> {
                    existingIncome.setReason(income.getReason());
                    existingIncome.setIncomeDate(LocalDate.parse(String.valueOf(income.getIncomeDate())));
                    existingIncome.setQuantity(income.getQuantity());
                    existingIncome.setAmountReceived(income.getAmountReceived());
                    existingIncome.setExpectedAmount(income.getExpectedAmount());
                    existingIncome.calculateDueBalance();
                    existingIncome.setReceipt(income.getReceipt());
                    existingIncome.setCreatedBy(income.getCreatedBy());

                    return incomeRepository.save(existingIncome);
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found")));
    }

    /**
     * Deletes an income record.
     *
     * @param id The ID of the income record to delete.
     * @return A Mono<Void> indicating completion.
     */
    @Override
    public Mono<Void> deleteIncome(String id) {
        if (!ObjectId.isValid(id)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format"));
        }

        return incomeRepository.existsById(id)
                .flatMap(exists -> exists
                        ? incomeRepository.deleteById(id)
                        : Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found")));
    }
}
