package com.akentech.shared.models.income.exception;

public class IncomeNotFoundException extends RuntimeException {
    public IncomeNotFoundException(String message) {
        super(message);
    }
}