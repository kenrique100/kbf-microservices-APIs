package com.akentech.kbf.transaction.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TransactionException extends RuntimeException {
    private final HttpStatus status;

    public TransactionException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}