package com.akentech.kbf.transaction.service.impl;

import com.akentech.kbf.kafka.utils.LoggingUtil;
import com.akentech.shared.models.Expense;
import com.akentech.shared.models.Income;
import com.akentech.shared.models.Investment;
import com.akentech.kbf.transaction.model.Transaction;
import com.akentech.kbf.transaction.repository.TransactionRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TransactionKafkaConsumer {

    private final TransactionRepository transactionRepository;

    public TransactionKafkaConsumer(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @KafkaListener(topics = "income-topic", groupId = "transaction-group")
    public void consumeIncome(Income income) {
        try {
            Transaction transaction = new Transaction("INCOME", income.getId().toString(), income.getIncomeDate(), income.getAmountReceived(), income.getCreatedBy());
            transactionRepository.save(transaction).subscribe();
            LoggingUtil.logInfo("Income transaction saved: " + income.getId());
        } catch (Exception e) {
            LoggingUtil.logError("Error processing income transaction: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "expense-topic", groupId = "transaction-group")
    public void consumeExpense(Expense expense) {
        try {
            Transaction transaction = new Transaction("EXPENSE", expense.getId().toString(), expense.getExpenseDate(), expense.getAmountPaid(), expense.getCreatedBy());
            transactionRepository.save(transaction).subscribe();
            LoggingUtil.logInfo("Expense transaction saved: " + expense.getId());
        } catch (Exception e) {
            LoggingUtil.logError("Error processing expense transaction: " + e.getMessage());
        }
    }

    @KafkaListener(topics = "investment-topic", groupId = "transaction-group")
    public void consumeInvestment(Investment investment) {
        try {
            Transaction transaction = new Transaction("INVESTMENT", investment.getId(), LocalDate.now(), investment.getCurrentBalance(), investment.getCreatedBy());
            transactionRepository.save(transaction).subscribe();
            LoggingUtil.logInfo("Investment transaction saved: " + investment.getId());
        } catch (Exception e) {
            LoggingUtil.logError("Error processing investment transaction: " + e.getMessage());
        }
    }
}