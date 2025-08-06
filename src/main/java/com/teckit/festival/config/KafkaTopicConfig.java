package com.teckit.festival.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic festivalEventTopic() {
        return TopicBuilder.name("festival-event")
                .partitions(1)  // 파티션 개수
                .replicas(1)    // 복제본 개수 (단일 서버면 1)
                .build();
    }
}
