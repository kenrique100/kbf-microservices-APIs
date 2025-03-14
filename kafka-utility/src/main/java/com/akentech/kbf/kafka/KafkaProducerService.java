package com.akentech.kbf.kafka;

import com.akentech.kbf.kafka.utils.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(String topic, Object message) {
        kafkaTemplate.send(topic, message)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        LoggingUtil.logInfo("Message sent successfully to topic: " + topic);
                    } else {
                        LoggingUtil.logError("Failed to send message to topic: " + topic + ", Error: " + ex.getMessage());
                    }
                });
    }
}