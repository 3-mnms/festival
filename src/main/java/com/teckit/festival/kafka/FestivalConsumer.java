package com.teckit.festival.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FestivalConsumer {

    @KafkaListener(topics = "festival-topic", groupId = "festival-group")
    public void consume(String message) {
        log.info("📥 Kafka Message Received: {}", message);
    }
}
