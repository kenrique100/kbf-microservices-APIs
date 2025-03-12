package com.akentech.shared.models.expense.repository;


import com.akentech.shared.models.Expense;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ExpenseRepository extends ReactiveMongoRepository<Expense, String> {
    Mono<Expense> findById(String id);
}