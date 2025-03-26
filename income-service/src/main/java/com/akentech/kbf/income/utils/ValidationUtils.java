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

public class ValidationUtils {

    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Validates the income ID.
     */
    public static void validateIncomeId(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid income ID. ID must be a positive number.");
        }
    }

    /**
     * Reads and validates income data from an Excel file.
     */
    public static List<Income> validateAndReadIncomeDataFromExcel(InputStream inputStream) throws IOException {
        List<Income> incomes = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                Income income = new Income();
                try {
                    // Read and set all fields
                    income.setIncomeDate(getCellValueAsLocalDate(row.getCell(0)));
                    income.setReason(getCellValueAsString(row.getCell(1)));
                    income.setQuantity(getCellValueAsInt(row.getCell(2)));
                    income.setAmountReceived(getCellValueAsBigDecimal(row.getCell(3)));
                    income.setExpectedAmount(getCellValueAsBigDecimal(row.getCell(4)));
                    income.setReceipt(getCellValueAsString(row.getCell(5)));
                    income.setCreatedBy(getCellValueAsString(row.getCell(6)));

                    // Calculate and validate
                    income.calculateDueBalance();
                    validateIncome(income);
                    incomes.add(income);
                } catch (Exception e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Error in row " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }
            }
        }
        return incomes;
    }


    private static LocalDate getCellValueAsLocalDate(Cell cell) {
        if (cell == null) {
            throw new ValidationException("Date cell cannot be empty");
        }
        try {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        } catch (Exception e) {
            throw new ValidationException("Invalid date format. Expected format: yyyy-MM-dd");
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            throw new ValidationException("Required cell is empty");
        }
        try {
            return cell.getStringCellValue().trim();
        } catch (Exception e) {
            throw new ValidationException("Invalid text value");
        }
    }

    private static int getCellValueAsInt(Cell cell) {
        if (cell == null) {
            throw new ValidationException("Quantity cell cannot be empty");
        }
        try {
            return (int) cell.getNumericCellValue();
        } catch (Exception e) {
            throw new ValidationException("Invalid quantity value");
        }
    }

    private static BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) {
            throw new ValidationException("Amount cell cannot be empty");
        }
        try {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } catch (Exception e) {
            throw new ValidationException("Invalid amount value");
        }
    }

    public static void validateIncome(Income income) {
        if (income == null) {
            throw new ValidationException("Income object cannot be null");
        }

        // Ensure due balance is calculated
        if (income.getDueBalance() == null) {
            income.calculateDueBalance();
        }

        List<String> errors = new ArrayList<>();

        // Validate required fields
        if (income.getIncomeDate() == null) errors.add("Income date is required");
        if (income.getReason() == null || income.getReason().trim().isEmpty()) errors.add("Reason is mandatory");
        if (income.getAmountReceived() == null) errors.add("Amount received is required");
        if (income.getExpectedAmount() == null) errors.add("Expected amount is required");
        if (income.getReceipt() == null || income.getReceipt().trim().isEmpty()) errors.add("Receipt is required");
        if (income.getCreatedBy() == null || income.getCreatedBy().trim().isEmpty()) errors.add("CreatedBy is required");

        // Validate field formats if values exist
        try {
            if (income.getIncomeDate() != null) validateIncomeDate(income.getIncomeDate());
        } catch (ResponseStatusException e) {
            errors.add(e.getReason());
        }

        try {
            if (income.getReason() != null) validateReason(income.getReason());
        } catch (ResponseStatusException e) {
            errors.add(e.getReason());
        }

        try {
            validateQuantity(income.getQuantity());
        } catch (ResponseStatusException e) {
            errors.add(e.getReason());
        }

        try {
            if (income.getAmountReceived() != null) validateAmountReceived(income.getAmountReceived());
        } catch (ResponseStatusException e) {
            errors.add(e.getReason());
        }

        try {
            if (income.getExpectedAmount() != null) validateExpectedAmount(income.getExpectedAmount());
        } catch (ResponseStatusException e) {
            errors.add(e.getReason());
        }

        try {
            if (income.getReceipt() != null) validateReceipt(income.getReceipt());
        } catch (ResponseStatusException e) {
            errors.add(e.getReason());
        }

        try {
            if (income.getCreatedBy() != null) validateCreatedBy(income.getCreatedBy());
        } catch (ResponseStatusException e) {
            errors.add(e.getReason());
        }

        // Business logic validation
        if (income.getExpectedAmount() != null &&
                income.getAmountReceived() != null &&
                income.getAmountReceived().compareTo(income.getExpectedAmount()) > 0) {
            errors.add("Amount received cannot be greater than expected amount");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(String.join("; ", errors));
        }
    }

    public static void validateIncomeDate(LocalDate incomeDate) {
        if (incomeDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Income date is required");
        }
        if (incomeDate.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Income date cannot be in the future. Expected format: yyyy-MM-dd");
        }
    }

    public static void validateReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reason is mandatory");
        }
        if (reason.length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Reason cannot exceed 255 characters");
        }
    }

    public static void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
        }
        if (quantity > 10000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Quantity cannot exceed 10,000");
        }
    }

    public static void validateAmountReceived(BigDecimal amountReceived) {
        if (amountReceived == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount received is required");
        }
        if (amountReceived.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Amount received must be positive");
        }
        if (amountReceived.compareTo(new BigDecimal("1000000")) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Amount received cannot exceed 1,000,000");
        }
    }

    public static void validateExpectedAmount(BigDecimal expectedAmount) {
        if (expectedAmount == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected amount is required");
        }
        if (expectedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Expected amount must be positive");
        }
    }

    public static void validateReceipt(String receipt) {
        if (receipt == null || receipt.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receipt cannot be empty");
        }
        if (!receipt.matches("^[A-Za-z0-9-]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Receipt can only contain letters, numbers and hyphens");
        }
    }

    public static void validateCreatedBy(String createdBy) {
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CreatedBy is mandatory");
        }
        if (createdBy.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "CreatedBy cannot exceed 100 characters");
        }
    }
}