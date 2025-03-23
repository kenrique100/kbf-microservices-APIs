package com.akentech.shared.models;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.beans.Transient;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Table("expense")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Expense {

    @Id
    private Long id;

    @NotBlank(message = "Reason is mandatory")
    private String reason;

    @NotNull(message = "Expense date is mandatory")
    private LocalDate expenseDate;

    @Min(value = 1, message = "Quantity Purchased must be at least 1")
    private int qtyPurchased;

    @Positive(message = "Amount Paid must be positive")
    private BigDecimal amountPaid;

    @Positive(message = "Expected amount must be positive")
    private BigDecimal expectedAmount;

    @PositiveOrZero(message = "Due balance must be positive or zero")
    private BigDecimal dueBalance;

    private String receipt;

    @NotBlank(message = "CreatedBy is mandatory")
    private String createdBy;

    @Transient // Prevents R2DBC from persisting this method
    public void calculateDueBalance() {
        if (this.expectedAmount != null && this.amountPaid != null) {
            this.dueBalance = this.expectedAmount.subtract(this.amountPaid);
        } else {
            this.dueBalance = BigDecimal.ZERO;
        }
    }
}
