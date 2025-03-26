package com.akentech.kbf.income.repository;

import com.akentech.shared.models.ProcessedDataIncome;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ProcessedDataIncomeRepository extends ReactiveCrudRepository<ProcessedDataIncome, Long> {
    @Modifying
    @Query("DELETE FROM processed_data_income WHERE income_id = :incomeId")
    Mono<Void> deleteByIncomeId(@Param("incomeId") Long incomeId);

    Mono<ProcessedDataIncome> findFirstByIncomeIdOrderByProcessedAtDesc(Long incomeId);
}