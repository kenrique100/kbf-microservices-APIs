package com.akentech.kbf.income.utils;

import com.akentech.shared.models.Income;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class ValidationUtils {

    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }

    public static void validateIncome(Income income) {
        if (income == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Income object cannot be null");
        }
        validateIncomeDate(income.getIncomeDate());
        validateReason(income.getReason());
        validateQuantity(income.getQuantity());
        validateAmountReceived(income.getAmountReceived());
        validateExpectedAmount(income.getExpectedAmount());
        validateReceipt(income.getReceipt());
        validateCreatedBy(income.getCreatedBy());
    }

    public static void validateIncomeId(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format");
        }
    }

    public static void validateIncomeDate(LocalDate incomeDate) {
        if (incomeDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Income date cannot be empty. Expected format: yyyy-MM-dd");
        }
    }

    public static void validateReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reason is mandatory");
        }
        if (!reason.matches("[a-zA-Z]+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reason must be a string.");
        }
    }

    public static void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
        }
    }

    public static void validateAmountReceived(BigDecimal amountReceived) {
        if (amountReceived == null || amountReceived.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount received must be positive");
        }
    }

    public static void validateExpectedAmount(BigDecimal expectedAmount) {
        if (expectedAmount == null || expectedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected amount must be positive");
        }
    }

    public static void validateReceipt(String receipt) {
        if (receipt == null || receipt.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receipt cannot be empty");
        }
    }

    public static void validateCreatedBy(String createdBy) {
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CreatedBy is mandatory");
        }
    }
}