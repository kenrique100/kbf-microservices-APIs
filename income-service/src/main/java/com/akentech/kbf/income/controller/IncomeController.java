package com.akentech.kbf.income.controller;

import com.akentech.kbf.income.exception.DuplicateIncomeException;
import com.akentech.kbf.income.kafka.producer.IncomeProducer;
import com.akentech.kbf.income.utils.FileProcessor;
import com.akentech.kbf.income.utils.ValidationUtils;
import com.akentech.shared.models.Income;
import com.akentech.kbf.income.service.IncomeService;
import com.akentech.shared.models.UploadResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@RestController
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
@Slf4j
public class IncomeController {
    private final IncomeService incomeService;
    private final IncomeProducer incomeProducer;
    private final FileProcessor fileProcessor;

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
        return incomeService.checkForDuplicate(income)
                .flatMap(isDuplicate -> {
                    if (isDuplicate) {
                        log.warn("Duplicate income detected: {}", income);
                        return Mono.error(new DuplicateIncomeException("Duplicate income detected"));
                    }
                    return incomeService.createIncome(income)
                            .doOnSuccess(savedIncome -> log.info("Income created with ID: {}", savedIncome.getId()))
                            .flatMap(incomeProducer::sendIncome);
                })
                .then();
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public Mono<UploadResult> uploadIncomeData(@RequestPart("file") FilePart filePart) {
        return fileProcessor.process(filePart)
                .map(processedCount -> {
                    log.info("File processed successfully. Records: {}", processedCount);
                    return new UploadResult(processedCount, "Upload successful");
                })
                .onErrorResume(e -> {
                    log.error("Error processing file upload: {}", e.getMessage());
                    return Mono.just(new UploadResult(0, "Upload failed: " + e.getMessage()));
                });
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