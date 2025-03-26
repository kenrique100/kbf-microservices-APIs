package com.akentech.kbf.income.repository;

import com.akentech.shared.models.Income;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import java.time.LocalDate;

@Repository
public interface IncomeRepository extends ReactiveCrudRepository<Income, Long> {
    Flux<Income> findByReasonAndIncomeDate(String reason, LocalDate incomeDate);
}