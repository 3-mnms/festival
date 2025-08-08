package com.teckit.festival.kafka;

import com.teckit.festival.dto.FestivalKafkaDTO;
import com.teckit.festival.entity.FestivalDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// FestivalKafkaProducer.java
@Slf4j
@Service
@RequiredArgsConstructor
public class FestivalKafkaProducer {


    private final KafkaTemplate<String, FestivalKafkaDTO> kafkaTemplate;

    public void send(FestivalDetail detail) {
        if (detail == null) {
            throw new IllegalArgumentException("FestivalDetail is null");
        }
        FestivalKafkaDTO dto = detail.toKafkaDTO();

        kafkaTemplate.send("festival-topic", dto)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("✅ Kafka 전송 성공: {}", dto);
                    } else {
                        log.error("❌ Kafka 전송 실패: {}", dto, ex);
                    }
                });

    }
}
