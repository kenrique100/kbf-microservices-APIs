package com.akentech.shared.models.investment.controller;

import com.akentech.shared.models.Investment;
import com.akentech.shared.models.InvestmentRequest;
import com.akentech.shared.models.UpdateInvestmentRequest;
import com.akentech.shared.models.investment.service.InvestmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/investments")
@RequiredArgsConstructor
public class InvestmentController {

    private final InvestmentService investmentService;

    @PostMapping
    public Mono<ResponseEntity<Investment>> createInvestment(@RequestBody @Valid InvestmentRequest request) {
        return investmentService.createInvestment(request.getInitialAmount(), request.getCreatedBy())
                .map(investment -> ResponseEntity.status(HttpStatus.CREATED).body(investment));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Investment>> getInvestmentById(@PathVariable @NotBlank(message = "ID cannot be blank") String id) {
        return investmentService.getInvestmentById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Flux<Investment>> getAllInvestments() {
        /* try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
        return ResponseEntity.ok(investmentService.getAllInvestments());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Investment>> updateInvestment(
            @PathVariable @NotBlank(message = "ID cannot be blank") String id,
            @RequestBody @Valid UpdateInvestmentRequest request) {
        return investmentService.updateInvestment(id, request.getNewAmount())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteInvestment(@PathVariable @NotBlank(message = "ID cannot be blank") String id) {
        return investmentService.deleteInvestment(id)
                .then(Mono.just(ResponseEntity.noContent().build()));
    }


    @PatchMapping("/{id}/deduct")
    public Mono<ResponseEntity<Investment>> deductFromInvestment(
            @PathVariable @NotBlank(message = "ID cannot be blank") String id,
            @RequestBody @Valid UpdateInvestmentRequest request) {
        return investmentService.deductFromInvestment(id, request.getNewAmount())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }


    @PatchMapping("/{id}/add")
    public Mono<ResponseEntity<Investment>> addToInvestment(
            @PathVariable @NotBlank(message = "ID cannot be blank") String id,
            @RequestBody @Valid UpdateInvestmentRequest request) {
        return investmentService.addToInvestment(id, request.getNewAmount())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/total-initial-amount")
    public Mono<ResponseEntity<BigDecimal>> getTotalInitialAmount() {
        return investmentService.getTotalInitialAmount()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/total-current-balance")
    public Mono<ResponseEntity<BigDecimal>> getTotalCurrentBalance() {
        return investmentService.getTotalCurrentBalance()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}