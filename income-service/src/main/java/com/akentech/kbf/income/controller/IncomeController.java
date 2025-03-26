package com.akentech.kbf.income.controller;

import com.akentech.kbf.income.exception.IncomeNotFoundException;
import com.akentech.kbf.income.kafka.producer.IncomeProducer;
import com.akentech.kbf.income.utils.PartInputStream;
import com.akentech.kbf.income.utils.ValidationUtils;
import com.akentech.shared.models.Income;
import com.akentech.kbf.income.service.IncomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;

@RestController
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
@Slf4j
public class IncomeController {

    private final IncomeService incomeService;
    private final IncomeProducer incomeProducer;

    @GetMapping
    public Flux<Income> getAllIncomes() {
        return incomeService.getAllIncomes();
    }

    @GetMapping("/{id}")
    public Mono<Income> getIncomeById(@PathVariable Long id) {
        ValidationUtils.validateIncomeId(id);
        return incomeService.getIncomeById(id)
                .switchIfEmpty(Mono.error(() -> new IncomeNotFoundException("Income not found with ID: " + id)))
                .doOnError(error -> log.error("Error fetching income with ID {}: {}", id, error.getMessage()));
    }
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> createIncome(@RequestBody @Valid Income income) {
        income.setStatus(Income.ProcessingStatus.PENDING.toString());
        return incomeProducer.sendIncome(income);
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> uploadIncomeData(@RequestPart("file") FilePart filePart) {
        return filePart.content()
                .collectList()
                .flatMapMany(dataBuffers -> {
                    try (InputStream is = new PartInputStream(dataBuffers)) {
                        return Flux.fromIterable(ValidationUtils.validateAndReadIncomeDataFromExcel(is))
                                .doOnNext(income -> income.setStatus(Income.ProcessingStatus.PENDING.toString()));
                    } catch (Exception e) {
                        return Flux.error(e);
                    }
                })
                .flatMap(incomeProducer::sendIncome)
                .then();
    }

    @PutMapping("/{id}")
    public Mono<Income> updateIncome(@PathVariable Long id, @RequestBody @Valid Income income) {
        ValidationUtils.validateIncomeId(id);
        ValidationUtils.validateIncome(income);
        return incomeService.updateIncome(id, income)
                .switchIfEmpty(Mono.error(() -> new IncomeNotFoundException("Income not found with ID: " + id)))
                .doOnError(error -> log.error("Error updating income with ID {}: {}", id, error.getMessage()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteIncome(@PathVariable Long id) {
        ValidationUtils.validateIncomeId(id);
        return incomeService.deleteIncome(id)
                .switchIfEmpty(Mono.error(() -> new IncomeNotFoundException("Income not found with ID: " + id)))
                .doOnError(error -> log.error("Error deleting income with ID {}: {}", id, error.getMessage()));
    }
}