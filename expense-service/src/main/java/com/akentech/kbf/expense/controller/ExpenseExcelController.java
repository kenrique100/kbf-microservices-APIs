package com.akentech.kbf.expense.controller;

import com.akentech.kbf.expense.utils.PartInputStream;
import com.akentech.kbf.expense.service.ExcelReaderService;
import com.akentech.kbf.expense.service.ExpenseService;
import com.akentech.shared.models.Expense;
import lombok.RequiredArgsConstructor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * Controller responsible for handling expense file uploads and processing.
 */
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseExcelController {

    private final ExcelReaderService excelReaderService;
    private final ExpenseService expenseService;

    /**
     * Handles the upload and processing of an Excel file containing expense data.
     *
     * @param filePart The uploaded Excel file.
     * @return A Mono<String> indicating the success or failure of processing.
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> uploadExpenseData(@RequestPart("file") FilePart filePart) {

        // Check if the uploaded file is empty
        if (filePart.headers().getContentLength() == 0) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty"));
        }

        // Validate file type (Only allow Excel files)
        if (!Objects.equals(filePart.headers().getContentType().toString(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type. Only Excel files are allowed."));
        }

        // Process the file asynchronously
        return filePart.content()
                .collectList()  // Collect all file content into a list of data buffers
                .flatMap(dataBuffers -> {
                    try (InputStream inputStream = new PartInputStream(dataBuffers)) {

                        // Read expenses from Excel file
                        List<Expense> expenses = excelReaderService.readExpenseDataFromExcel(inputStream);

                        // Process expenses sequentially and save them to the database
                        return Flux.fromIterable(expenses)
                                .concatMap(expenseService::createExpense)  // Ensures ordered, one-by-one processing
                                .collectList()  // Collect all saved expenses into a list
                                .map(savedExpenses -> "Successfully processed " + savedExpenses.size() + " records.");

                    } catch (IOException e) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to read file: " + e.getMessage()));
                    }
                })
                // Handle any unexpected errors
                .onErrorResume(e -> Mono.error(new ResponseStatusException(
                        e instanceof IOException ? HttpStatus.BAD_REQUEST :
                                e instanceof InvalidFormatException ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR,
                        e.getMessage())));
    }
}
