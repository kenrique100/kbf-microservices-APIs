package com.akentech.shared.models;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("income")
public class Income {

    @Id
    private Long id;

    @NotBlank(message = "Reason is mandatory")
    private String reason;

    @NotNull(message = "Income date is mandatory")
    private LocalDate incomeDate;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @Positive(message = "Amount received must be positive")
    private BigDecimal amountReceived;

    @Positive(message = "Expected amount must be positive")
    private BigDecimal expectedAmount;

    @PositiveOrZero(message = "Due balance must be positive or zero")
    private BigDecimal dueBalance;

    @NotBlank(message = "Receipt is mandatory")
    private String receipt;

    @NotBlank(message = "CreatedBy is mandatory")
    private String createdBy;

    public void calculateDueBalance() {
        if (this.expectedAmount != null && this.amountReceived != null) {
            this.dueBalance = this.expectedAmount.subtract(this.amountReceived);
        } else {
            this.dueBalance = BigDecimal.ZERO;
        }
    }
}