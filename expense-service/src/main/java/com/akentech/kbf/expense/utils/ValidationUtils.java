package com.akentech.kbf.expense.utils;

import com.akentech.shared.models.Expense;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ValidationUtils {

    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }

    public static void validateExpense(Expense expense) {
        if (expense == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expense object cannot be null");
        }
        validateReason(expense.getReason());
        validateExpenseDate(expense.getExpenseDate());
        validateQuantity(expense.getQtyPurchased());
        validateAmountPaid(expense.getAmountPaid());
        validateExpectedAmount(expense.getExpectedAmount());
        validateReceipt(expense.getReceipt());
        validateCreatedBy(expense.getCreatedBy());
    }

    public static void validateReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reason is mandatory");
        }
    }

    public static void validateExpenseDate(LocalDate expenseDate) {
        if (expenseDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expense date is mandatory");
        }
    }

    public static void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
        }
    }

    public static void validateAmountPaid(BigDecimal amountPaid) {
        if (amountPaid == null || amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount paid must be positive");
        }
    }

    public static void validateExpectedAmount(BigDecimal expectedAmount) {
        if (expectedAmount == null || expectedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected amount must be positive");
        }
    }

    public static void validateReceipt(String receipt) {
        if (receipt == null || receipt.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receipt is mandatory");
        }
    }
    public static void validateExpenseId(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format");
        }
    }

    public static void validateCreatedBy(String createdBy) {
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CreatedBy is mandatory");
        }
    }
}