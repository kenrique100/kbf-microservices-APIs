package com.akentech.kbf.income.repository;

import com.akentech.shared.models.Income;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface IncomeRepository extends ReactiveCrudRepository<Income, Long> {
    Flux<Income> findByStatus(String status);
    Mono<Boolean> existsByReasonAndIncomeDateAndAmountReceived(
            String reason,
            LocalDate incomeDate,
            BigDecimal amountReceived
    );
}