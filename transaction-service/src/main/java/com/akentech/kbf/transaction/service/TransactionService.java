package com.akentech.kbf.transaction.service;

import com.akentech.kbf.transaction.model.dto.DashboardReportDTO;
import reactor.core.publisher.Mono;

public interface TransactionService {
    Mono<DashboardReportDTO> getDashboardData(int range);
}