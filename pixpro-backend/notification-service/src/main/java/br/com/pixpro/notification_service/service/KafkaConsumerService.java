package br.com.pixpro.notification_service.service;

import br.com.pixpro.notification_service.handler.NotificationWebSocketHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final NotificationWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    public KafkaConsumerService(NotificationWebSocketHandler webSocketHandler, ObjectMapper objectMapper) {
        this.webSocketHandler = webSocketHandler;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "image-processing-results", groupId = "notification-service-group")
    public void consumeImageProcessingResult(String message) {
        logger.info("<<< Mensagem de resultado de processamento recebida: {}", message);
        try {
            // 1. Converte a mensagem JSON (string) para um Map
            Map<String, Object> resultData = objectMapper.readValue(message, new TypeReference<>() {});

            // 2. Extrai o ID do usuário da mensagem
            Long userId = ((Number) resultData.get("userId")).longValue();

            // 3. Monta uma mensagem de notificação para o frontend
            Map<String, Object> notificationPayload = Map.of(
                    "type", "PROCESSING_UPDATE",
                    "imageId", resultData.get("imageId"),
                    "status", resultData.get("status")
            );
            String notificationJson = objectMapper.writeValueAsString(notificationPayload);

            // 4. Chama o handler para enviar a mensagem via WebSocket para o usuário específico
            webSocketHandler.sendMessageToUser(userId, notificationJson);

        } catch (Exception e) {
            logger.error("!!! Falha ao processar ou enviar notificação para a mensagem: {}", message, e);
        }
    }

}