package br.com.pixpro.notification_service.config;

import br.com.pixpro.notification_service.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);
    private final JwtService jwtService;

    public JwtHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        String query = request.getURI().getQuery();
        if (query != null && query.startsWith("token=")) {
            String token = query.substring(6);
            try {
                if (jwtService.isTokenValid(token)) {
                    String userEmail = jwtService.extractUsername(token);
                    Long userId = jwtService.extractClaim(token, claims -> claims.get("userId", Long.class));

                    // Se o token for válido, adicionamos o userId aos atributos da sessão.
                    // O WebSocketHandler terá acesso a isso.
                    attributes.put("userId", userId);
                    logger.info("Handshake WebSocket bem-sucedido para o usuário {}", userEmail);
                    return true; // Permite a conexão
                }
            } catch (Exception e) {
                logger.error("!!! Falha na validação do token JWT no handshake: {}", e.getMessage());
            }
        }

        logger.warn("!!! Handshake WebSocket rejeitado: token ausente ou inválido.");
        return false; // Rejeita a conexão
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // Nenhuma ação necessária após o handshake
    }
}