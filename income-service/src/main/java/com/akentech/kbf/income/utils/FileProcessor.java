package com.akentech.kbf.income.utils;

import com.akentech.kbf.income.service.IncomeService;
import com.akentech.kbf.income.kafka.producer.IncomeProducer;
import com.akentech.shared.models.Income;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileProcessor {
    private final IncomeService incomeService;
    private final IncomeProducer incomeProducer;

    public Mono<Integer> process(FilePart filePart) {
        return filePart.content()
                .collectList()
                .flatMapMany(dataBuffers -> {
                    try (InputStream is = new PartInputStream(dataBuffers)) {
                        List<Income> incomes = ValidationUtils.readIncomeDataFromExcel(is);
                        return Flux.fromIterable(incomes)
                                .doOnNext(income -> income.setStatus(Income.ProcessingStatus.PENDING.toString()));
                    } catch (Exception e) {
                        log.error("Error processing Excel file", e);
                        return Flux.error(new RuntimeException("Error processing Excel file: " + e.getMessage()));
                    }
                })
                .filterWhen(income -> incomeService.checkForDuplicate(income)
                        .map(isDuplicate -> !isDuplicate))
                .flatMap(income -> incomeService.createIncome(income)
                        .flatMap(incomeProducer::sendIncome))
                .count()
                .map(Long::intValue);
    }
}