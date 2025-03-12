package com.akentech.shared.models.transaction.model.dto;

import com.akentech.shared.models.transaction.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardReportDTO {
    private List<Transaction> transactions;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal totalInvestment;
    private BigDecimal netGain;
    private BigDecimal netLoss;

}