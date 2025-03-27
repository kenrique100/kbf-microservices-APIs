package com.akentech.kbf.income.kafka.producer;

import com.akentech.shared.models.Income;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionProducer {
    private final KafkaTemplate<String, Income> kafkaTemplate;

    public Mono<Void> sendTransaction(Income income) {
        return Mono.fromCallable(() -> {
                    CompletableFuture<SendResult<String, Income>> future =
                            kafkaTemplate.send("income-processed-topic", income);

                    return future.handle((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send income transaction to Kafka: {}", ex.getMessage());
                            throw new RuntimeException("Kafka transaction send failed", ex);
                        }
                        log.info("Sent income transaction to Kafka. Offset: {}", result.getRecordMetadata().offset());
                        return result;
                    });
                })
                .flatMap(Mono::fromFuture)
                .then();
    }
}