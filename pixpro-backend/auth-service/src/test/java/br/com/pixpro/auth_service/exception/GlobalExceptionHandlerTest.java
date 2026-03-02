package br.com.pixpro.auth_service.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final WebRequest webRequest = mock(WebRequest.class);

    @Test
    @DisplayName("handleEmailAlreadyExistsException returns 400 with message")
    void handleEmailAlreadyExistsException_ReturnsBadRequest() {
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException("Email já cadastrado.");

        ResponseEntity<Object> response = handler.handleEmailAlreadyExistsException(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Email já cadastrado.", body.get("message"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertTrue(body.containsKey("timestamp"));
    }

    @Test
    @DisplayName("handleUserNotFoundException returns 404 with message")
    void handleUserNotFoundException_ReturnsNotFound() {
        UserNotFoundException ex = new UserNotFoundException("Usuário não encontrado após autenticação.");

        ResponseEntity<Object> response = handler.handleUserNotFoundException(ex, webRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Usuário não encontrado após autenticação.", body.get("message"));
        assertEquals(HttpStatus.NOT_FOUND.value(), body.get("status"));
        assertTrue(body.containsKey("timestamp"));
    }

//    @Test
//    @DisplayName("handleBadCredentialsException returns 401 with fixed message")
//    void handleBadCredentialsException_ReturnsUnauthorized() {
//        org.springframework.security.authentication.BadCredentialsException ex =
//                new org.springframework.security.authentication.BadCredentialsException("bad creds");
//
//        ResponseEntity<Object> response = handler.handleBadCredentialsException(ex, webRequest);
//
//        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
//        @SuppressWarnings("unchecked")
//        Map<String, Object> body = (Map<String, Object>) response.getBody();
//        assertEquals("Credenciais inválidas. Verifique seu e-mail e senha.", body.get("message"));
//        assertEquals(HttpStatus.UNAUTHORIZED.value(), body.get("status"));
//        assertTrue(body.containsKey("timestamp"));
//    }

    @Test
    @DisplayName("handleAccessDeniedException returns 403 with fixed message")
    void handleAccessDeniedException_ReturnsForbidden() {
        org.springframework.security.access.AccessDeniedException ex =
                new org.springframework.security.access.AccessDeniedException("denied");

        ResponseEntity<Object> response = handler.handleAccessDeniedException(ex, webRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Acesso negado. Você não tem permissão para acessar este recurso.", body.get("message"));
        assertEquals(HttpStatus.FORBIDDEN.value(), body.get("status"));
        assertTrue(body.containsKey("timestamp"));
    }

    @Test
    @DisplayName("handleGlobalException returns 500 with generic message")
    void handleGlobalException_ReturnsInternalServerError() {
        Exception ex = new Exception("boom");

        ResponseEntity<Object> response = handler.handleGlobalException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Ocorreu um erro inesperado no servidor.", body.get("message"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.get("status"));
        assertTrue(body.containsKey("timestamp"));
    }
}
