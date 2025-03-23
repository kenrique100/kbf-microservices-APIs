package com.akentech.kbf.income.repository;

import com.akentech.shared.models.Income;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomeRepository extends ReactiveCrudRepository<Income, Long> {
}