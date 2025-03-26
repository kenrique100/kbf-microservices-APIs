package com.akentech.shared.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("processed_data_income")
public class ProcessedDataIncome {
    @Id
    private Long id;
    private Long incomeId;
    @Builder.Default
    private BigDecimal processedAmount = BigDecimal.ZERO;
    private LocalDate processedAt;
    private String processedBy;
    private String status;
    private String errorMessage;
    @Builder.Default
    private Integer attempts = 0;
}