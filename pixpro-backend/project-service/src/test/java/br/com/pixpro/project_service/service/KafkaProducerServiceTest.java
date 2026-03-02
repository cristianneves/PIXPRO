package br.com.pixpro.project_service.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    @Test
    @DisplayName("Should send message to Kafka topic")
    void sendImageProcessingRequest_ValidTopicAndMessage_SendsSuccessfully() {
        // Arrange
        String topic = "image-processing-queue";
        String message = "{\"imageId\":123,\"userId\":456}";

        // Act
        kafkaProducerService.sendImageProcessingRequest(topic, message);

        // Assert
        verify(kafkaTemplate).send(eq(topic), eq(message));
    }
}
