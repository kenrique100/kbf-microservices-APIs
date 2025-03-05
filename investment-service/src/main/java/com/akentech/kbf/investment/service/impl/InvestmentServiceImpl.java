package com.akentech.kbf.investment.service.impl;

import com.akentech.kbf.investment.exception.InsufficientBalanceException;
import com.akentech.kbf.investment.exception.InvalidRequestException;
import com.akentech.kbf.investment.model.Investment;
import com.akentech.kbf.investment.repository.InvestmentRepository;
import com.akentech.kbf.investment.service.InvestmentService;
import com.akentech.kbf.investment.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class InvestmentServiceImpl implements InvestmentService {

    private final InvestmentRepository investmentRepository;

    @Override
    public Mono<Investment> createInvestment(BigDecimal initialAmount, String createdBy) {
        if (initialAmount == null || initialAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new InvalidRequestException("Initial amount must be greater than zero"));
        }
        if (createdBy == null || createdBy.isBlank()) {
            return Mono.error(new InvalidRequestException("CreatedBy cannot be null or empty"));
        }

        Investment investment = Investment.builder()
                .initialAmount(initialAmount)
                .currentBalance(initialAmount)
                .createdBy(createdBy)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        return investmentRepository.save(investment);
    }

    @Override
    public Mono<Investment> getInvestmentById(String id) {
        return ValidationUtil.validateId(id)
                .flatMap(investmentRepository::findById)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Investment not found with ID: " + id)));
    }

    @Override
    public Flux<Investment> getAllInvestments() {
        return investmentRepository.findAll();
    }

    @Override
    public Mono<Investment> updateInvestment(String id, BigDecimal newAmount) {
        if (newAmount == null || newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new InvalidRequestException("New amount must be greater than zero"));
        }

        return ValidationUtil.validateId(id)
                .flatMap(investmentRepository::findById)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Investment not found with ID: " + id)))
                .flatMap(investment -> {
                    BigDecimal balanceDifference = newAmount.subtract(investment.getInitialAmount());
                    investment.setInitialAmount(newAmount);
                    investment.setCurrentBalance(investment.getCurrentBalance().add(balanceDifference));
                    investment.setUpdatedAt(LocalDate.now());
                    return investmentRepository.save(investment);
                });
    }

    @Override
    public Mono<Void> deleteInvestment(String id) {
        return ValidationUtil.validateId(id)
                .flatMap(investmentRepository::findById)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Investment not found with ID: " + id)))
                .flatMap(existingInvestment -> investmentRepository.deleteById(id));
    }

    @Override
    public Mono<Investment> deductFromInvestment(String id, BigDecimal amount) {
        return ValidationUtil.validateIdAndAmount(id, amount)
                .flatMap(investmentRepository::findById)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Investment not found with ID: " + id)))
                .flatMap(investment -> {
                    if (investment.getCurrentBalance().compareTo(amount) < 0) {
                        return Mono.error(new InsufficientBalanceException("Insufficient investment balance"));
                    }
                    investment.setCurrentBalance(investment.getCurrentBalance().subtract(amount));
                    investment.setUpdatedAt(LocalDate.now());
                    return investmentRepository.save(investment);
                });
    }

    @Override
    public Mono<Investment> addToInvestment(String id, BigDecimal amount) {
        return ValidationUtil.validateIdAndAmount(id, amount)
                .flatMap(investmentRepository::findById)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Investment not found with ID: " + id)))
                .flatMap(investment -> {
                    investment.setCurrentBalance(investment.getCurrentBalance().add(amount));
                    investment.setUpdatedAt(LocalDate.now());
                    return investmentRepository.save(investment);
                });
    }

    @Override
    public Mono<BigDecimal> getTotalInitialAmount() {
        return investmentRepository.findAll()
                .map(Investment::getInitialAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Mono<BigDecimal> getTotalCurrentBalance() {
        return investmentRepository.findAll()
                .map(Investment::getCurrentBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}