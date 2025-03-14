package com.akentech.kbf.transaction.controller;

import com.akentech.kbf.transaction.exception.TransactionException;
import com.akentech.kbf.transaction.model.dto.DashboardReportDTO;
import com.akentech.kbf.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/dashboard")
    public Mono<ResponseEntity<DashboardReportDTO>> getDashboardData(@RequestParam int range) {
        return transactionService.getDashboardData(range)
                .map(ResponseEntity::ok)
                .onErrorResume(TransactionException.class, e -> Mono.just(ResponseEntity.status(e.getStatus()).body(null)))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)));
    }
}