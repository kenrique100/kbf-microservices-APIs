package com.akentech.kbf.income.utils;

import com.akentech.shared.models.Income;
import jakarta.validation.ValidationException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ValidationUtils {
    private static final int MAX_REASON_LENGTH = 255;
    private static final int MAX_CREATED_BY_LENGTH = 100;
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000");
    private static final int MAX_QUANTITY = 10000;

    private ValidationUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static void validateIncomeId(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid income ID. ID must be a positive number."
            );
        }
    }

    public static List<Income> readIncomeDataFromExcel(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "Input stream cannot be null");

        List<Income> incomes = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header row

                try {
                    Income income = mapRowToIncome(row);
                    incomes.add(income);
                } catch (Exception e) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Error in row " + (row.getRowNum() + 1) + ": " + e.getMessage()
                    );
                }
            }
        }
        return incomes;
    }

    private static Income mapRowToIncome(Row row) {
        Income income = new Income();
        income.setReason(ExcelUtil.getCellValueAsString(row.getCell(1)));
        income.setIncomeDate(ExcelUtil.getCellValueAsLocalDate(row.getCell(0)));
        income.setQuantity(ExcelUtil.getCellValueAsInt(row.getCell(2)));
        income.setAmountReceived(ExcelUtil.getCellValueAsBigDecimal(row.getCell(3)));
        income.setExpectedAmount(ExcelUtil.getCellValueAsBigDecimal(row.getCell(4)));
        income.setReceipt(ExcelUtil.getCellValueAsString(row.getCell(5)));
        income.setCreatedBy(ExcelUtil.getCellValueAsString(row.getCell(6)));
        income.calculateDueBalance();

        validateIncome(income);
        return income;
    }

    public static void validateIncome(Income income) {
        Objects.requireNonNull(income, "Income object cannot be null");

        List<String> errors = new ArrayList<>();

        // Required fields validation
        validateField(income.getReason(), "Reason", errors);
        validateField(income.getIncomeDate(), "Income date", errors);
        validateField(income.getAmountReceived(), "Amount received", errors);
        validateField(income.getExpectedAmount(), "Expected amount", errors);
        validateField(income.getReceipt(), "Receipt", errors);
        validateField(income.getCreatedBy(), "CreatedBy", errors);

        // Field-specific validation
        validateReason(income.getReason(), errors);
        validateIncomeDate(income.getIncomeDate(), errors);
        validateQuantity(income.getQuantity(), errors);
        validateAmount(income.getAmountReceived(), "Amount received", errors);
        validateAmount(income.getExpectedAmount(), "Expected amount", errors);
        validateReceipt(income.getReceipt(), errors);
        validateCreatedBy(income.getCreatedBy(), errors);

        // Business logic validation
        if (income.getExpectedAmount() != null && income.getAmountReceived() != null &&
                income.getAmountReceived().compareTo(income.getExpectedAmount()) > 0) {
            errors.add("Amount received cannot be greater than expected amount");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(String.join("; ", errors));
        }

        // Ensure due balance is calculated
        if (income.getDueBalance() == null) {
            income.calculateDueBalance();
        }
    }

    private static void validateField(Object field, String fieldName, List<String> errors) {
        if (field == null || (field instanceof String && ((String) field).trim().isEmpty())) {
            errors.add(fieldName + " is required");
        }
    }

    private static void validateIncomeDate(LocalDate date, List<String> errors) {
        if (date != null && date.isAfter(LocalDate.now())) {
            errors.add("Income date cannot be in the future");
        }
    }

    private static void validateReason(String reason, List<String> errors) {
        if (reason != null && reason.length() > MAX_REASON_LENGTH) {
            errors.add("Reason cannot exceed " + MAX_REASON_LENGTH + " characters");
        }
    }

    private static void validateQuantity(int quantity, List<String> errors) {
        if (quantity < 1) {
            errors.add("Quantity must be at least 1");
        }
        if (quantity > MAX_QUANTITY) {
            errors.add("Quantity cannot exceed " + MAX_QUANTITY);
        }
    }

    private static void validateAmount(BigDecimal amount, String fieldName, List<String> errors) {
        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                errors.add(fieldName + " must be positive");
            }
            if (amount.compareTo(MAX_AMOUNT) > 0) {
                errors.add(fieldName + " cannot exceed " + MAX_AMOUNT);
            }
        }
    }

    private static void validateReceipt(String receipt, List<String> errors) {
        if (receipt != null && !receipt.matches("^[A-Za-z0-9-]+$")) {
            errors.add("Receipt can only contain letters, numbers and hyphens");
        }
    }

    private static void validateCreatedBy(String createdBy, List<String> errors) {
        if (createdBy != null && createdBy.length() > MAX_CREATED_BY_LENGTH) {
            errors.add("CreatedBy cannot exceed " + MAX_CREATED_BY_LENGTH + " characters");
        }
    }
}