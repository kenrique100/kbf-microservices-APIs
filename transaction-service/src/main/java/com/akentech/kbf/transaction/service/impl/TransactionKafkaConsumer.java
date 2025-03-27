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

    @KafkaListener(topics = "income-processed-topic", groupId = "transaction-group")
    public Mono<Void> consumeIncome(Income income) {
        return Mono.just(income)
                .filter(i -> Income.ProcessingStatus.SUCCESS.name().equals(i.getStatus()))
                .flatMap(validIncome -> transactionRepository.save(
                        new Transaction(
                                "INCOME",
                                validIncome.getId().toString(),
                                validIncome.getIncomeDate(),
                                validIncome.getAmountReceived(),
                                validIncome.getCreatedBy()
                        )
                ))
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