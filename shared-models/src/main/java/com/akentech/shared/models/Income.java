package com.akentech.shared.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("income")
public class Income {
    @Id
    private Long id;
    private String reason;
    private LocalDate incomeDate;
    private int quantity;
    private BigDecimal amountReceived;
    private BigDecimal expectedAmount;
    private BigDecimal dueBalance;
    private String receipt;
    private String createdBy;
    private String status;
    private String errorMessage;
    private LocalDateTime createdDate;

    public enum ProcessingStatus {
        PENDING,
        PROCESSING,
        SUCCESS,
        FAILED,
        RETRYING
    }

    public void calculateDueBalance() {
        if (this.expectedAmount != null && this.amountReceived != null) {
            this.dueBalance = this.expectedAmount.subtract(this.amountReceived);
        } else {
            this.dueBalance = BigDecimal.ZERO;
        }
    }
}