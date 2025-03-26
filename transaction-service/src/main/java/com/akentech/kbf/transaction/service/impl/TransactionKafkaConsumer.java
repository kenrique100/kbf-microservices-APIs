package com.akentech.kbf.transaction.service.impl;

import com.akentech.shared.models.Expense;
import com.akentech.shared.models.Income;
import com.akentech.shared.models.Investment;
import com.akentech.kbf.transaction.model.Transaction;
import com.akentech.kbf.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionKafkaConsumer {
    private final TransactionRepository transactionRepository;

    @KafkaListener(topics = "income-transaction-topic", groupId = "transaction-group")
    public Mono<Void> consumeIncome(Income income) {
        // Only create transaction if income is successful
        if (!Income.ProcessingStatus.SUCCESS.name().equals(income.getStatus())) {
            log.warn("Skipping transaction creation for non-success income: {}", income.getId());
            return Mono.empty();
        }

        return transactionRepository.save(
                        new Transaction(
                                "INCOME",
                                income.getId().toString(),
                                income.getIncomeDate(),
                                income.getAmountReceived(),
                                income.getCreatedBy()
                        )
                )
                .doOnSuccess(t -> log.info("Income transaction saved: {}", income.getId()))
                .doOnError(e -> log.error("Error processing income transaction: {}", e.getMessage()))
                .then();
    }

    @KafkaListener(topics = "expense-topic", groupId = "transaction-group")
    public Mono<Void> consumeExpense(Expense expense) {
        return transactionRepository.save(
                        new Transaction(
                                "EXPENSE",
                                expense.getId().toString(),
                                expense.getExpenseDate(),
                                expense.getAmountPaid(),
                                expense.getCreatedBy()
                        )
                )
                .doOnSuccess(t -> log.info("Expense transaction saved: {}", expense.getId()))
                .doOnError(e -> log.error("Error processing expense transaction: {}", e.getMessage()))
                .then();
    }

    @KafkaListener(topics = "investment-topic", groupId = "transaction-group")
    public Mono<Void> consumeInvestment(Investment investment) {
        return transactionRepository.save(
                        new Transaction(
                                "INVESTMENT",
                                investment.getId().toString(),
                                LocalDate.now(),
                                investment.getCurrentBalance(),
                                investment.getCreatedBy()
                        )
                )
                .doOnSuccess(t -> log.info("Investment transaction saved: {}", investment.getId()))
                .doOnError(e -> log.error("Error processing investment transaction: {}", e.getMessage()))
                .then();
    }
}