package br.com.pixpro.notification_service.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationWebSocketHandlerTest {

    @Test
    @DisplayName("afterConnectionEstablished stores session when userId present")
    void afterConnectionEstablished_WithUserId() throws Exception {
        NotificationWebSocketHandler handler = new NotificationWebSocketHandler();
        WebSocketSession session = mock(WebSocketSession.class);
        Map<String,Object> attrs = new HashMap<>();
        attrs.put("userId", 55L);
        when(session.getAttributes()).thenReturn(attrs);

        handler.afterConnectionEstablished(session);

        // invoke send and verify delegate call
        when(session.isOpen()).thenReturn(true);
        ArgumentCaptor<TextMessage> textCaptor = ArgumentCaptor.forClass(TextMessage.class);

        handler.sendMessageToUser(55L, "{\"msg\":\"hi\"}");
        verify(session).sendMessage(textCaptor.capture());
        assertEquals("{\"msg\":\"hi\"}", textCaptor.getValue().getPayload());
    }

    @Test
    @DisplayName("afterConnectionEstablished closes session when userId missing")
    void afterConnectionEstablished_NoUserId() throws Exception {
        NotificationWebSocketHandler handler = new NotificationWebSocketHandler();
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getAttributes()).thenReturn(Map.of());

        handler.afterConnectionEstablished(session);
        verify(session).close(any(CloseStatus.class));
    }

    @Test
    @DisplayName("afterConnectionClosed removes stored session")
    void afterConnectionClosed_RemovesSession() throws Exception {
        NotificationWebSocketHandler handler = new NotificationWebSocketHandler();
        WebSocketSession session = mock(WebSocketSession.class);
        Map<String,Object> attrs = new HashMap<>();
        attrs.put("userId", 99L);
        when(session.getAttributes()).thenReturn(attrs);
        when(session.isOpen()).thenReturn(true);

        handler.afterConnectionEstablished(session);
        handler.afterConnectionClosed(session, CloseStatus.NORMAL);

        // attempt to send after removal should NOT call sendMessage on session
        handler.sendMessageToUser(99L, "{}");
        verify(session, never()).sendMessage(any());
    }

    @Test
    @DisplayName("sendMessageToUser warns silently when session not present")
    void sendMessageToUser_NoSession_NoError() {
        NotificationWebSocketHandler handler = new NotificationWebSocketHandler();
        // Should simply do nothing
        handler.sendMessageToUser(777L, "{}");
        // No assertions - just ensure no exception thrown
    }
}
