package com.akentech.kbf.investment.repository;

import com.akentech.shared.models.Investment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;


public interface InvestmentRepository extends ReactiveMongoRepository<Investment, String> {
}