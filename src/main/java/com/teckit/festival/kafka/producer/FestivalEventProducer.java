package com.teckit.festival.kafka.producer;

import com.teckit.festival.dto.FestivalEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FestivalEventProducer {

    private final KafkaTemplate<String, FestivalEventDTO> kafkaTemplate;

    @Value("${app.kafka.topic.festival-event}")
    private String topic;

    public void send(FestivalEventDTO dto) {
        kafkaTemplate.send(topic, dto.getFestivalId(), dto);
    }
}
