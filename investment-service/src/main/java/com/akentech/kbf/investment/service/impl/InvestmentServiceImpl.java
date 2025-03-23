package com.akentech.kbf.investment.service.impl;

import com.akentech.kbf.investment.exception.InsufficientBalanceException;
import com.akentech.kbf.investment.exception.InvalidRequestException;
import com.akentech.kbf.investment.repository.InvestmentRepository;
import com.akentech.kbf.investment.service.InvestmentService;
import com.akentech.kbf.investment.utils.ValidationUtils;
import com.akentech.shared.models.Investment;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class InvestmentServiceImpl implements InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Mono<Investment> createInvestment(BigDecimal initialAmount, String createdBy) {
        ValidationUtils.validateInitialAmount(initialAmount);
        ValidationUtils.validateCreatedBy(createdBy);

        Investment investment = Investment.builder()
                .initialAmount(initialAmount)
                .currentBalance(initialAmount)
                .createdBy(createdBy)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        return investmentRepository.save(investment)
                .doOnSuccess(savedInvestment -> kafkaTemplate.send("investment-topic", savedInvestment.getId().toString(), savedInvestment));
    }

    @Override
    public Mono<Investment> getInvestmentById(Long id) {
        return investmentRepository.findById(id)
                .switchIfEmpty(Mono.error(new InvalidRequestException("Investment not found with ID: " + id)));
    }

    @Override
    public Flux<Investment> getAllInvestments() {
        return investmentRepository.findAll();
    }

    @Override
    public Mono<Investment> updateInvestment(Long id, BigDecimal newAmount) {
        ValidationUtils.validateAmount(newAmount);

        return investmentRepository.findById(id)
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
    public Mono<Void> deleteInvestment(Long id) {
        return investmentRepository.existsById(id)
                .flatMap(exists -> exists
                        ? investmentRepository.deleteById(id)
                        : Mono.error(new InvalidRequestException("Investment not found with ID: " + id)));
    }

    @Override
    public Mono<Investment> deductFromInvestment(Long id, BigDecimal amount) {
        ValidationUtils.validateAmount(amount);

        return investmentRepository.findById(id)
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
    public Mono<Investment> addToInvestment(Long id, BigDecimal amount) {
        ValidationUtils.validateAmount(amount);

        return investmentRepository.findById(id)
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