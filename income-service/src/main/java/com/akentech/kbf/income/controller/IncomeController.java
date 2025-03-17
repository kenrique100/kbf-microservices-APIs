package com.akentech.kbf.income.controller;

import com.akentech.shared.models.Income;
import com.akentech.kbf.income.service.IncomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * REST controller for managing income records.
 */
@RestController
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
@Slf4j
public class IncomeController {

    private final IncomeService incomeService;
    /**
     * Retrieves all income records.
     *
     * @return Flux of Income objects.
     */

    @GetMapping
    public Flux<Income> getAllIncomes() {
        /* try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
        return incomeService.getAllIncomes();
    }

    /**
     * Retrieves a specific income by ID.
     *
     * @param id The ID of the income record.
     * @return Mono of Income if found, otherwise an error.
     */
    @GetMapping("/{id}")
    public Mono<Income> getIncomeById(@PathVariable String id) {
        return incomeService.getIncomeById(id)
                .doOnError(error -> log.error("Error fetching income with ID {}: {}", id, error.getMessage()));
    }

    /**
     * Creates a new income record.
     *
     * @param income The income object to create.
     * @return Mono of the created Income.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Income> createIncome(@RequestBody @Valid Income income) {
        return incomeService.createIncome(income)
                .doOnError(error -> log.error("Error creating income: {}", error.getMessage()));
    }

    /**
     * Updates an existing income record.
     *
     * @param id The ID of the income record to update.
     * @param income The updated income object.
     * @return Mono of the updated Income if found, otherwise an error.
     */
    @PutMapping("/{id}")
    public Mono<Income> updateIncome(@PathVariable String id, @RequestBody Income income) {
        return incomeService.updateIncome(id, income)
                .doOnError(error -> log.error("Error updating income with ID {}: {}", id, error.getMessage()));
    }

    /**
     * Deletes an income record by ID.
     *
     * @param id The ID of the income record to delete.
     * @return Mono<Void> indicating success or error if the record is not found.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteIncome(@PathVariable String id) {
        return incomeService.deleteIncome(id)
                .doOnError(error -> log.error("Error deleting income with ID {}: {}", id, error.getMessage()));
    }
}