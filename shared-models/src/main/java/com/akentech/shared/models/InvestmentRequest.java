package com.akentech.shared.models;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentRequest {
    @NotNull(message = "Initial amount is required")
    @DecimalMin(value = "1.0", message = "Initial amount must be greater than zero")
    private BigDecimal initialAmount;

    @NotBlank(message = "Created by field is required")
    private String createdBy;
}