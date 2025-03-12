package com.akentech.shared.models.investment.service;

import com.akentech.shared.models.Investment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface InvestmentService {
    Mono<Investment> createInvestment(BigDecimal initialAmount, String createdBy);
    Mono<Investment> getInvestmentById(String id);
    Flux<Investment> getAllInvestments();
    Mono<Investment> updateInvestment(String id, BigDecimal newAmount);
    Mono<Void> deleteInvestment(String id);
    Mono<Investment> deductFromInvestment(String id, BigDecimal amount);
    Mono<Investment> addToInvestment(String id, BigDecimal amount);
    Mono<BigDecimal> getTotalInitialAmount();
    Mono<BigDecimal> getTotalCurrentBalance();
}