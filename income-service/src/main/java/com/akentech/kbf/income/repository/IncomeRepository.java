package com.akentech.kbf.income.repository;

import com.akentech.shared.models.Income;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface IncomeRepository extends ReactiveMongoRepository<Income, String> {
    Mono<Income> findById(String id);
}