package com.akentech.kbf.investment.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "investments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Investment {
    @Id
    private String id;

    @NotNull(message = "Initial amount is required")
    @DecimalMin(value = "1.0", message = "Initial amount must be greater than zero")
    private BigDecimal initialAmount;

    @NotNull(message = "Current balance is required")
    @DecimalMin(value = "0.0", message = "Current balance cannot be negative")
    private BigDecimal currentBalance;

    @NotBlank(message = "Created by field is required")
    private String createdBy;

    @PastOrPresent(message = "Created date cannot be in the future")
    private LocalDate createdAt;

    @PastOrPresent(message = "Updated date cannot be in the future")
    private LocalDate updatedAt;
}