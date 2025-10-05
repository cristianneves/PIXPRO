package br.com.pixpro.notification_service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

    // Um mapa thread-safe para armazenar as sessões ativas, ligando o userId à sessão.
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // O userId será extraído do token JWT durante o handshake e colocado nos atributos da sessão.
        Long userId = (Long) session.getAttributes().get("userId");

        if (userId != null) {
            sessions.put(userId, session);
            logger.info(">>> Conexão WebSocket estabelecida para o usuário: {}", userId);
        } else {
            logger.warn("!!! Tentativa de conexão WebSocket sem userId. Fechando a sessão.");
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.remove(userId);
            logger.info("<<< Conexão WebSocket fechada para o usuário: {}. Motivo: {}", userId, status);
        }
    }

    /**
     * Envia uma mensagem para um usuário específico, se ele estiver conectado.
     * @param userId O ID do usuário a ser notificado.
     * @param payload A mensagem (JSON em formato de string) a ser enviada.
     */
    public void sendMessageToUser(Long userId, String payload) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                logger.info(">>> Enviando notificação para o usuário {}: {}", userId, payload);
                session.sendMessage(new TextMessage(payload));
            } catch (IOException e) {
                logger.error("!!! Falha ao enviar mensagem para o usuário {}", userId, e);
            }
        } else {
            logger.warn("! Nenhuma sessão WebSocket ativa encontrada para o usuário {}", userId);
        }
    }
}