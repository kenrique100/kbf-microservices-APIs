package com.akentech.kbf.income.service;

import com.akentech.shared.models.Income;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface IncomeService {
    Flux<Income> getAllIncomes();
    Mono<Income> getIncomeById(Long id);
    Mono<Income> createIncome(Income income);
    Mono<Income> updateIncome(Long id, Income income);
    Mono<Void> deleteIncome(Long id);
}