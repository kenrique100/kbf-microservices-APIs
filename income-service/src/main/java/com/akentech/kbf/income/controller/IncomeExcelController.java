package com.akentech.kbf.income.controller;

import com.akentech.kbf.income.service.ExcelReaderService;
import com.akentech.kbf.income.service.IncomeService;
import com.akentech.kbf.income.util.PartInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@RestController
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
@Slf4j
public class IncomeExcelController {

    private final ExcelReaderService excelReaderService;
    private final IncomeService incomeService;

    /**
     * Uploads an Excel file and processes the income data asynchronously.
     *
     * @param filePart The Excel file to upload as a Part.
     * @return A Mono<String> indicating success or failure.
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> uploadIncomeData(@RequestPart("file") FilePart filePart) {
        log.info("Received file upload request: {}", filePart.filename());

        // Validate if the file is empty
        if (filePart.headers().getContentLength() == 0) {
            log.error("File is empty");
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty"));
        }

        // Validate file type (e.g., ensure it's an Excel file)
        if (filePart.headers().getContentType() == null || !Objects.equals(filePart.headers().getContentType().toString(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            log.error("Invalid file type: {}", filePart.headers().getContentType());
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type. Only Excel files are allowed."));
        }

        return filePart.content()
                .collectList()
                .flatMap(dataBuffers -> {
                    try (InputStream inputStream = new PartInputStream(dataBuffers)) {
                        log.info("Processing file: {}", filePart.filename());
                        return Mono.just(excelReaderService.readIncomeDataFromExcel(inputStream)); // Wrap in Mono.just
                    } catch (IOException e) {
                        log.error("Failed to read file: {}", e.getMessage());
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read file: " + e.getMessage()));
                    }
                })
                .flatMapMany(Flux::fromIterable) // Convert List<Income> to Flux<Income>
                .flatMap(incomeService::createIncome) // Save each income record asynchronously
                .collectList() // Collect all saved records
                .map(savedList -> {
                    log.info("Successfully processed {} income records from file: {}", savedList.size(), filePart.filename());
                    return "Successfully processed " + savedList.size() + " income records.";
                })
                .doOnError(error -> {
                    log.error("Failed to process Excel file: {}", error.getMessage());
                    if (error instanceof IOException) {
                        log.error("File reading error: {}", error.getMessage());
                    } else if (error instanceof RuntimeException) {
                        log.error("Processing error: {}", error.getMessage());
                    }
                })
                .onErrorResume(e -> {
                    if (e instanceof IOException) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "File reading error: " + e.getMessage()));
                    } else if (e instanceof InvalidFormatException) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Excel file format: " + e.getMessage()));
                    } else {
                        return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process file: " + e.getMessage()));
                    }
                });
    }
}