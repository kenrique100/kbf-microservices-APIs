package com.akentech.kbf.income.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    @ExceptionHandler(InvalidFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidFormatException(InvalidFormatException ex) {
        if (ex.getTargetType() != null && ex.getTargetType().equals(LocalDate.class)) {
            return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid date format for 'incomeDate'. Expected format: yyyy-MM-dd");
        }
        if (ex.getTargetType() != null && ex.getTargetType().equals(int.class)) {
            return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid format for 'quantity'. Quantity must be a number.");
        }
        if (ex.getTargetType() != null && ex.getTargetType().equals(BigDecimal.class)) {
            return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid format for '" + ex.getPath().getFirst().getFieldName() + "'. Must be a valid number.");
        }
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Invalid format for field: " + ex.getPath().getFirst().getFieldName());
    }

    @ExceptionHandler(IncomeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleIncomeNotFoundException(IncomeNotFoundException ex) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Unexpected error: " + ex.getMessage());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(WebExchangeBindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseEntity<>(new ErrorResponse(status.value(), ex.getReason()), status);
    }
}