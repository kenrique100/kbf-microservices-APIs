package com.akentech.kbf.income.repository;

import com.akentech.shared.models.ProcessedDataIncome;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;


@Repository
public interface ProcessedDataIncomeRepository extends ReactiveCrudRepository<ProcessedDataIncome, Long> {
    Mono<ProcessedDataIncome> findFirstByIncomeIdOrderByProcessedAtDesc(Long incomeId);
}