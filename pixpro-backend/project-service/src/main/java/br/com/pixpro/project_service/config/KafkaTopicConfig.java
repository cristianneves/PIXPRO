package br.com.pixpro.project_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic imageProcessingTopic() {
        return TopicBuilder.name("image-processing-queue")
                .build();
    }

    @Bean
    public NewTopic imageResultsTopic() {
        return TopicBuilder.name("image-processing-results").build();
    }
}