package com.akentech.kbf.investment.utils;

import com.akentech.kbf.investment.exception.InvalidRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

public class ValidationUtils {

    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }

    public static void validateInitialAmount(BigDecimal initialAmount) {
        if (initialAmount == null || initialAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Initial amount must be greater than zero");
        }
    }

    public static void validateCreatedBy(String createdBy) {
        if (createdBy == null || createdBy.isBlank()) {
            throw new InvalidRequestException("CreatedBy cannot be null or empty");
        }
    }
    public static void validateInvestmentId(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ID format");
        }
    }

    public static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Amount must be greater than zero");
        }
    }
}