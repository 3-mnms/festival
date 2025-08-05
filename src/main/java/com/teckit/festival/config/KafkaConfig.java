package com.teckit.festival.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic festivalTopic() {
        return new NewTopic("festival-topic", 1, (short) 1);
    }
}
