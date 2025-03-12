package com.akentech.shared.models.investment.utils;

import com.akentech.shared.models.investment.exception.InvalidRequestException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public class ValidationUtil {

    private ValidationUtil() {
        // Private constructor to prevent instantiation
    }

    public static Mono<String> validateId(String id) {
        return (id == null || id.isBlank())
                ? Mono.error(new InvalidRequestException("ID cannot be null or empty"))
                : Mono.just(id);
    }

    public static Mono<String> validateIdAndAmount(String id, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new InvalidRequestException("Amount must be greater than zero"));
        }
        return validateId(id);
    }
}