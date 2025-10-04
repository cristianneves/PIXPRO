package br.com.pixpro.project_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendImageProcessingRequest(String topic, String message) {
        logger.info(">>> Enviando mensagem para o tópico Kafka [{}]: {}", topic, message);
        kafkaTemplate.send(topic, message);
    }
}