package com.akentech.kbf.transaction.repository;

import com.akentech.kbf.transaction.model.Transaction;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends ReactiveCrudRepository<Transaction, Long> {
}