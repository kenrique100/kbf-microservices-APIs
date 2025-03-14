package com.akentech.kbf.income.service;

import com.akentech.shared.models.Income;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IncomeService {
    Flux<Income> getAllIncomes();
    Mono<Income> getIncomeById(String id);
    Mono<Income> createIncome(Income income);
    Mono<Income> updateIncome(String id, Income income);
    Mono<Void> deleteIncome(String id);
}