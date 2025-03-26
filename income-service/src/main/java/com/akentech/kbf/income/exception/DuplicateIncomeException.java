package com.akentech.kbf.income.exception;

public class DuplicateIncomeException extends RuntimeException {
    public DuplicateIncomeException(String message) {
        super(message);
    }
}