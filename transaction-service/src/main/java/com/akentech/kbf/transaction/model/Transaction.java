package com.akentech.kbf.transaction.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("transaction")
public class Transaction {
    @Id
    private Long id;

    private String type; // INCOME, EXPENSE, INVESTMENT
    private String referenceId; // ID of the income, expense, or investment
    private LocalDate transactionDate;
    private BigDecimal amount;
    private String createdBy;

    public Transaction(String type, String referenceId, LocalDate transactionDate, BigDecimal amount, String createdBy) {
        this.type = type;
        this.referenceId = referenceId;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.createdBy = createdBy;
    }
}