package com.akentech.kbf.expense.repository;

import com.akentech.shared.models.Expense;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ExpenseRepository extends R2dbcRepository<Expense, Long> {
    Mono<Expense> findById(Long id);
}
