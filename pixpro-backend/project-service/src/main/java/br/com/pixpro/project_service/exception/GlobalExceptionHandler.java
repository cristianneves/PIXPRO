package br.com.pixpro.project_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 1. Erros de Validação de Campos (@Valid) - Retorna detalhes de cada campo
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", "Erro de validação nos campos enviados.");

        // Cria um mapa de field -> erro (ex: "name" -> "não pode estar em branco")
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 2. Erro de Arquivo Faltando
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Object> handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST,
                "O arquivo obrigatório '" + ex.getRequestPartName() + "' não foi enviado.");
    }

    // 3. Erro de Upload Muito Grande (Limite do Spring)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return buildErrorResponse(HttpStatus.EXPECTATION_FAILED,
                "Arquivo muito grande! O tamanho máximo permitido foi excedido.");
    }

    // 4. Erros de Regra de Negócio (Nossas exceções personalizadas)
    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<Object> handleProjectNotFoundException(ProjectNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<Object> handleImageNotFoundException(ImageNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 5. Erros de Segurança (Acesso Negado)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Acesso negado. Você não tem permissão para este recurso.");
    }

    // 6. Erros do MinIO (S3)
    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<Object> handleS3Exception(S3Exception ex) {
        logger.error("!!! Erro de Armazenamento (MinIO/S3): ", ex);
        // Retorna 503 (Service Unavailable) pois o problema é na infraestrutura
        return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE,
                "Erro ao comunicar com o serviço de armazenamento. Tente novamente mais tarde.");
    }

    // 7. Erros de Integridade do Banco (ex: chave duplicada, nulo onde não pode)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        logger.error("!!! Erro de Integridade de Dados: ", ex);
        return buildErrorResponse(HttpStatus.CONFLICT,
                "Conflito de dados. Operação não permitida (verifique duplicidades ou campos obrigatórios).");
    }

    // 8. Erros de JSON Malformado
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Corpo da requisição JSON inválido ou mal formatado.");
    }

    // 9. Handler Genérico (Fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("!!! ERRO CRÍTICO NÃO TRATADO: ", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno inesperado.");
    }

    // Método auxiliar para construir a resposta JSON padrão
    private ResponseEntity<Object> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}