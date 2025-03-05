package com.akentech.kbf.expense.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.bson.types.ObjectId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(value = "expense")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Expense {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;

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

    public void calculateDueBalance() {
        if (this.expectedAmount != null && this.amountPaid != null) {
            this.dueBalance = this.expectedAmount.subtract(this.amountPaid);
        } else {
            this.dueBalance = BigDecimal.ZERO;
        }
    }
}