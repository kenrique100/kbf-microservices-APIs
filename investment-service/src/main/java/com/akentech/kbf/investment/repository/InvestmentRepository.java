package com.akentech.kbf.investment.repository;

import com.akentech.shared.models.Investment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface InvestmentRepository extends ReactiveCrudRepository<Investment, Long> {
    Mono<Investment> findById(Long id);
}