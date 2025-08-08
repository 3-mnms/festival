package com.teckit.festival.kafka.producer;

import com.teckit.festival.dto.FestivalEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FestivalEventProducer {

    private final KafkaTemplate<String, FestivalEventDTO> kafkaTemplate;

    public void send(FestivalEventDTO dto) {
        kafkaTemplate.send("festival-events", dto);
    }
}
