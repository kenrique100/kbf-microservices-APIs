package com.akentech.shared.models.transaction.repository;

import com.akentech.shared.models.transaction.model.Transaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface TransactionRepository extends ReactiveMongoRepository<Transaction, String> {
    Flux<Transaction> findByType(String type);
}