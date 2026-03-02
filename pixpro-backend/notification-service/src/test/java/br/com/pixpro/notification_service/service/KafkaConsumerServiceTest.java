package br.com.pixpro.notification_service.service;

import br.com.pixpro.notification_service.handler.NotificationWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Map;

class KafkaConsumerServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("consumeImageProcessingResult parses kafka message and notifies user via WebSocket")
    void consumeImageProcessingResult_SendsWebSocket() throws Exception {
        NotificationWebSocketHandler handler = mock(NotificationWebSocketHandler.class);
        KafkaConsumerService service = new KafkaConsumerService(handler, objectMapper);

        String message = objectMapper.writeValueAsString(Map.of(
                "userId", 123,
                "imageId", "img-abc",
                "status", "DONE"
        ));

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        service.consumeImageProcessingResult(message);

        verify(handler, times(1)).sendMessageToUser(eq(123L), payloadCaptor.capture());

        // verify payload content
        String payloadJson = payloadCaptor.getValue();
        Map<?,?> payload = objectMapper.readValue(payloadJson, Map.class);
        assertEquals("PROCESSING_UPDATE", payload.get("type"));
        assertEquals("img-abc", payload.get("imageId"));
        assertEquals("DONE", payload.get("status"));
    }

    @Test
    @DisplayName("consumeImageProcessingResult tolerates invalid json and does not throw")
    void consumeImageProcessingResult_InvalidJson_NoThrow() {
        NotificationWebSocketHandler handler = mock(NotificationWebSocketHandler.class);
        KafkaConsumerService service = new KafkaConsumerService(handler, objectMapper);

        // invalid JSON
        service.consumeImageProcessingResult("{invalid-json");

        verify(handler, never()).sendMessageToUser(anyLong(), anyString());
    }
}
