package com.teckit.festival.kafka;

import com.teckit.festival.dto.FestivalKafkaDTO;
import com.teckit.festival.entity.FestivalDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FestivalKafkaProducer {

    private static final String TOPIC = "festival-topic";

    private final KafkaTemplate<String, FestivalKafkaDTO> kafkaTemplate;

    /** 등록/수정 공용 전송 */
    public void send(FestivalDetail detail, String eventType) {
        if (detail == null) throw new IllegalArgumentException("FestivalDetail is null");
        FestivalKafkaDTO dto = detail.toKafkaDTO();
        // ✅ DTO 스펙에 맞춰 eventType만 세팅
        dto.setEventType(eventType);

        // 키는 fid(id)로
        String key = dto.getId();

        kafkaTemplate.send(TOPIC, key, dto)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        // log.info("✅ Kafka 전송 성공 [{}]: {}", eventType, dto);
                    } else {
                        log.error("❌ Kafka 전송 실패 [{}]: {}", eventType, dto, ex);
                    }
                });
    }

    /** 삭제 전용 전송 */
    public void sendDeleted(String fid) {
        if (fid == null || fid.isBlank()) throw new IllegalArgumentException("fid is required");

        FestivalKafkaDTO dto = FestivalKafkaDTO.builder()
                .eventType("FESTIVAL_DELETED")
                .id(fid)
                // 나머지 필드는 null/기본값으로 전송
                .build();

        kafkaTemplate.send(TOPIC, fid, dto)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        // log.info("🗑️ Kafka 삭제 이벤트 전송: {}", dto);
                    } else {
                        log.error("❌ Kafka 삭제 이벤트 전송 실패: {}", dto, ex);
                    }
                });
    }
}
