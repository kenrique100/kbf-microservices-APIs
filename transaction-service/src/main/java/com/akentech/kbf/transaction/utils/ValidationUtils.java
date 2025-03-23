package com.akentech.kbf.transaction.utils;

import com.akentech.kbf.transaction.exception.TransactionException;
import org.springframework.http.HttpStatus;

public class ValidationUtils {

    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }

    public static void validateRange(int range) {
        if (range <= 0) {
            throw new TransactionException("Range must be greater than zero", HttpStatus.BAD_REQUEST);
        }
    }
}