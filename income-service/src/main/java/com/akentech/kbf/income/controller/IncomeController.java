package com.akentech.kbf.income.controller;

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
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.io.InputStream;
import java.util.List;

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

    @GetMapping("/pending")
    public Flux<Income> getPendingIncomes() {
        return incomeService.getPendingIncomes();
    }

    @GetMapping("/failed")
    public Flux<Income> getFailedIncomes() {
        return incomeService.getFailedIncomes();
    }

    @GetMapping("/{id}")
    public Mono<Income> getIncomeById(@PathVariable Long id) {
        ValidationUtils.validateIncomeId(id);
        return incomeService.getIncomeById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> createIncome(@RequestBody @Valid Income income) {
        return incomeService.createIncome(income)
                .flatMap(incomeProducer::sendIncome)
                .then();
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> uploadIncomeData(@RequestPart("file") FilePart filePart) {
        return filePart.content()
                .collectList()
                .flatMapMany(dataBuffers -> {
                    try (InputStream is = new PartInputStream(dataBuffers)) {
                        List<Income> incomes = ValidationUtils.validateAndReadIncomeDataFromExcel(is);
                        return Flux.fromIterable(incomes)
                                .doOnNext(income -> income.setStatus(Income.ProcessingStatus.PENDING.toString()));
                    } catch (Exception e) {
                        return Flux.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Error processing file: " + e.getMessage()));
                    }
                })
                .flatMap(income -> incomeService.createIncome(income)
                        .flatMap(incomeProducer::sendIncome))
                .then();
    }

    @PutMapping("/{id}")
    public Mono<Income> updateIncome(@PathVariable Long id, @RequestBody @Valid Income income) {
        ValidationUtils.validateIncomeId(id);
        return incomeService.updateIncome(id, income);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteIncome(@PathVariable Long id) {
        ValidationUtils.validateIncomeId(id);
        return incomeService.deleteIncome(id);
    }

    @PostMapping("/{id}/retry")
    public Mono<Income> retryFailedIncome(@PathVariable Long id) {
        ValidationUtils.validateIncomeId(id);
        return incomeService.retryFailedIncome(id);
    }
}