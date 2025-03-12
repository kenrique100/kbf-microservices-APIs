package com.akentech.shared.models.transaction.service;

import com.akentech.shared.models.transaction.model.dto.DashboardReportDTO;
import reactor.core.publisher.Mono;

public interface TransactionService {
    Mono<DashboardReportDTO> getDashboardData(int range);
}