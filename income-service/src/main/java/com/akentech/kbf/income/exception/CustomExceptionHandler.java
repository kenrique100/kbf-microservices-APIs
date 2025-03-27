package com.akentech.kbf.income.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler(InvalidFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidFormatException(InvalidFormatException ex) {
        String message = "Invalid format for field: " + ex.getPath().getFirst().getFieldName();

        if (ex.getTargetType() != null) {
            if (ex.getTargetType().equals(LocalDate.class)) {
                message = "Invalid date format for 'incomeDate'. Expected format: yyyy-MM-dd";
            } else if (ex.getTargetType().equals(int.class)) {
                message = "Invalid format for 'quantity'. Quantity must be a number.";
            } else if (ex.getTargetType().equals(BigDecimal.class)) {
                message = "Invalid format for '" + ex.getPath().getFirst().getFieldName() + "'. Must be a valid number.";
            }
        }

        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .errorCode("INVALID_FORMAT")
                .timestamp(Instant.now())
                .build();
    }

    @ExceptionHandler(IncomeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleIncomeNotFoundException(IncomeNotFoundException ex) {
        return ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .errorCode("INCOME_NOT_FOUND")
                .timestamp(Instant.now())
                .build();
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(WebExchangeBindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed for one or more fields.")
                .errorCode("VALIDATION_ERROR")
                .timestamp(Instant.now())
                .details(errors)
                .build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .status(status.value())
                        .message(ex.getReason())
                        .errorCode("HTTP_STATUS_EXCEPTION")
                        .timestamp(Instant.now())
                        .build());
    }

    @ExceptionHandler(DuplicateIncomeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateIncomeException(DuplicateIncomeException ex) {
        log.warn("Duplicate income detected: {}", ex.getMessage());

        return ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .errorCode("DUPLICATE_INCOME")
                .timestamp(Instant.now())
                .build();
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(ValidationException ex) {
        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .errorCode("VALIDATION_ERROR")
                .timestamp(Instant.now())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);

        return ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred.")
                .errorCode("INTERNAL_SERVER_ERROR")
                .timestamp(Instant.now())
                .build();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Cannot delete: Record is referenced by other data";
        if (ex.getMessage() != null && ex.getMessage().contains("constraint")) {
            message = "Cannot delete: Record is used in other transactions";
        }

        return ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message)
                .errorCode("DATA_INTEGRITY_VIOLATION")
                .timestamp(Instant.now())
                .build();
    }

}
