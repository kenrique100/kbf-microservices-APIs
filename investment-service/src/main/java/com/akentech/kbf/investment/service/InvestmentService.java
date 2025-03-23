package com.akentech.kbf.investment.service;

import com.akentech.shared.models.Investment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface InvestmentService {
    Mono<Investment> createInvestment(BigDecimal initialAmount, String createdBy);
    Mono<Investment> getInvestmentById(Long id);
    Flux<Investment> getAllInvestments();
    Mono<Investment> updateInvestment(Long id, BigDecimal newAmount);
    Mono<Void> deleteInvestment(Long id);
    Mono<Investment> deductFromInvestment(Long id, BigDecimal amount);
    Mono<Investment> addToInvestment(Long id, BigDecimal amount);
    Mono<BigDecimal> getTotalInitialAmount();
    Mono<BigDecimal> getTotalCurrentBalance();
}