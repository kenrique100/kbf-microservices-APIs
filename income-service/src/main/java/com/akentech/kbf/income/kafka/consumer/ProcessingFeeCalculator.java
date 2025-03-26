package com.akentech.kbf.income.kafka.consumer;

import com.akentech.shared.models.Income;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class ProcessingFeeCalculator {

    private static final BigDecimal PROCESSING_FEE_PERCENT = new BigDecimal("0.02"); // 2%

    public BigDecimal calculateNetAmount(Income income) {
        BigDecimal fee = income.getAmountReceived().multiply(PROCESSING_FEE_PERCENT);
        BigDecimal netAmount = income.getAmountReceived().subtract(fee);
        log.info("Calculated net amount: {} (Fee: {})", netAmount, fee);
        return netAmount;
    }
}
