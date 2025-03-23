package com.akentech.kbf.income.service.impl;

import com.akentech.kbf.income.repository.IncomeRepository;
import com.akentech.kbf.income.service.IncomeService;
import com.akentech.kbf.income.utils.ValidationUtils;
import com.akentech.shared.models.Income;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public Flux<Income> getAllIncomes() {
        return incomeRepository.findAll();
    }

    @Override
    public Mono<Income> getIncomeById(Long id) {
        return incomeRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found")));
    }

    @Override
    public Mono<Income> createIncome(Income income) {
        ValidationUtils.validateIncome(income);
        income.calculateDueBalance();

        return incomeRepository.save(income)
                .map(savedIncome -> {
                    kafkaTemplate.send("income-topic", savedIncome.getId().toString(), savedIncome);
                    return savedIncome;
                });
    }

    @Override
    public Mono<Income> updateIncome(Long id, Income income) {
        return incomeRepository.findById(id)
                .flatMap(existingIncome -> {
                    existingIncome.setReason(income.getReason());
                    existingIncome.setIncomeDate(income.getIncomeDate());
                    existingIncome.setQuantity(income.getQuantity());
                    existingIncome.setAmountReceived(income.getAmountReceived());
                    existingIncome.setExpectedAmount(income.getExpectedAmount());
                    existingIncome.calculateDueBalance();
                    existingIncome.setReceipt(income.getReceipt());
                    existingIncome.setCreatedBy(income.getCreatedBy());

                    return incomeRepository.save(existingIncome);
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found")));
    }

    @Override
    public Mono<Void> deleteIncome(Long id) {
        return incomeRepository.existsById(id)
                .flatMap(exists -> exists
                        ? incomeRepository.deleteById(id)
                        : Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found")));
    }
}