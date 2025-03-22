/*
package com.akentech.kbf.income.controller;

import com.akentech.kbf.income.service.IncomeService;
import com.akentech.kbf.income.utils.PartInputStream;
import com.akentech.kbf.income.utils.ValidationUtils;
import com.akentech.shared.models.Income;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
@Slf4j
public class IncomeExcelController {

    private final IncomeService incomeService;

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> uploadIncomeData(@RequestPart("file") FilePart filePart) {
        log.info("Received file upload request: {}", filePart.filename());

        if (filePart.headers().getContentLength() == 0) {
            log.error("File is empty");
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty"));
        }

        if (filePart.headers().getContentType() == null ||
                !Objects.equals(filePart.headers().getContentType().toString(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            log.error("Invalid file type: {}", filePart.headers().getContentType());
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type. Only Excel files are allowed."));
        }

        return filePart.content()
                .collectList()
                .flatMap(dataBuffers -> {
                    try (InputStream inputStream = new PartInputStream(dataBuffers)) {
                        List<Income> incomes = ValidationUtils.validateAndReadIncomeDataFromExcel(inputStream);
                        log.info("Read {} income records from file", incomes.size());

                        return Flux.fromIterable(incomes)
                                .concatMap(income -> {
                                    try {
                                        // Validate the income object before saving
                                        ValidationUtils.validateIncome(income);
                                        return incomeService.createIncome(income);
                                    } catch (ResponseStatusException e) {
                                        log.error("Validation error for income record: {}", e.getMessage());
                                        return Mono.error(e);
                                    }
                                })
                                .collectList()
                                .map(savedIncomes -> {
                                    log.info("Successfully processed {} income records.", savedIncomes.size());
                                    return "Successfully processed " + savedIncomes.size() + " records.";
                                });

                    } catch (IOException e) {
                        log.error("Failed to read file: {}", e.getMessage());
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read file: " + e.getMessage()));
                    } catch (DateTimeParseException e) {
                        log.error("Invalid date format in Excel file: {}", e.getMessage());
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format in Excel file. Expected format: yyyy-MM-dd"));
                    } catch (Exception e) {
                        log.error("Failed to process Excel file: {}", e.getMessage());
                        return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process file: " + e.getMessage()));
                    }
                })
                .doOnError(error -> log.error("Failed to process Excel file: {}", error.getMessage()));
    }
}
*/
