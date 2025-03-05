package com.akentech.kbf.transaction.service.impl;

import com.akentech.kbf.income.model.Income;
import com.akentech.kbf.expense.model.Expense;
import com.akentech.kbf.investment.model.Investment;
import com.akentech.kbf.transaction.exception.TransactionException;
import com.akentech.kbf.transaction.model.dto.DashboardReportDTO;
import com.akentech.kbf.transaction.model.Transaction;
import com.akentech.kbf.transaction.repository.TransactionRepository;
import com.akentech.kbf.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final WebClient incomeWebClient;
    private final WebClient expenseWebClient;
    private final WebClient investmentWebClient;
    private final TransactionRepository transactionRepository;

    @Override
    public Mono<DashboardReportDTO> getDashboardData(int range) {
        return Flux.merge(
                        incomeWebClient.get()
                                .uri("/api/incomes")
                                .retrieve()
                                .bodyToFlux(Income.class)
                                .onErrorResume(e -> {
                                    log.error("Error fetching incomes: {}", e.getMessage());
                                    return Flux.empty();
                                })
                                .map(income -> new Transaction("INCOME", income.getId().toString(), income.getIncomeDate(), income.getAmountReceived(), income.getCreatedBy())),

                        expenseWebClient.get()
                                .uri("/api/expenses")
                                .retrieve()
                                .bodyToFlux(Expense.class)
                                .onErrorResume(e -> {
                                    log.error("Error fetching expenses: {}", e.getMessage());
                                    return Flux.empty();
                                })
                                .map(expense -> new Transaction("EXPENSE", expense.getId().toString(), expense.getExpenseDate(), expense.getAmountPaid(), expense.getCreatedBy())),

                        investmentWebClient.get()
                                .uri("/api/investments")
                                .retrieve()
                                .bodyToFlux(Investment.class)
                                .onErrorResume(e -> {
                                    log.error("Error fetching investments: {}", e.getMessage());
                                    return Flux.empty();
                                })
                                .map(investment -> new Transaction("INVESTMENT", investment.getId(), LocalDate.now(), investment.getCurrentBalance(), investment.getCreatedBy()))
                )
                .collectList()
                .flatMap(transactions -> {
                    if (transactions.isEmpty()) {
                        log.warn("No transactions found for the given range: {}", range);
                        return Mono.error(new TransactionException("No transactions found", HttpStatus.NOT_FOUND));
                    }

                    // Save transactions to the database
                    return transactionRepository.saveAll(transactions)
                            .collectList()
                            .flatMap(savedTransactions -> {
                                BigDecimal totalIncome = savedTransactions.stream()
                                        .filter(t -> "INCOME".equals(t.getType()))
                                        .map(Transaction::getAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                BigDecimal totalExpense = savedTransactions.stream()
                                        .filter(t -> "EXPENSE".equals(t.getType()))
                                        .map(Transaction::getAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                                BigDecimal totalInvestment = savedTransactions.stream()
                                        .filter(t -> "INVESTMENT".equals(t.getType()))
                                        .map(Transaction::getAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);


                                BigDecimal netGainLoss = totalIncome.subtract(totalExpense).subtract(totalInvestment);
                                BigDecimal netGain = netGainLoss.compareTo(BigDecimal.ZERO) > 0 ? netGainLoss : BigDecimal.ZERO;
                                BigDecimal netLoss = netGainLoss.compareTo(BigDecimal.ZERO) < 0 ? netGainLoss.abs() : BigDecimal.ZERO;

                                return Mono.just(new DashboardReportDTO(
                                        savedTransactions,
                                        totalIncome,
                                        totalExpense,
                                        totalInvestment,
                                        netGain,
                                        netLoss
                                ));
                            });
                })
                .onErrorResume(e -> {
                    log.error("Error generating dashboard data: {}", e.getMessage());
                    return Mono.error(new TransactionException("Error generating dashboard data", HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }
}