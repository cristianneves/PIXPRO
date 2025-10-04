package br.com.pixpro.project_service.service;

import br.com.pixpro.project_service.model.ImageMetadata;
import br.com.pixpro.project_service.model.ProcessingStatus;
import br.com.pixpro.project_service.repository.ImageMetadataRepository;
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
    private final ImageMetadataRepository imageMetadataRepository;
    private final ObjectMapper objectMapper;

    public KafkaConsumerService(ImageMetadataRepository imageMetadataRepository, ObjectMapper objectMapper) {
        this.imageMetadataRepository = imageMetadataRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "image-processing-results", groupId = "project-service-group-1")
    public void consumeImageProcessingResult(String message) {
        try {
            logger.info("<<< Mensagem de resultado recebida: {}", message);
            Map<String, Object> result = objectMapper.readValue(message, new TypeReference<>() {});

            Long imageId = ((Number) result.get("imageId")).longValue();
            String status = (String) result.get("status");
            String processedPath = (String) result.get("processedStoragePath");

            ImageMetadata metadata = imageMetadataRepository.findById(imageId)
                    .orElseThrow(() -> new RuntimeException("Metadados da imagem não encontrados para o ID: " + imageId));

            metadata.setStatus(ProcessingStatus.valueOf(status));
            metadata.setProcessedStoragePath(processedPath);

            imageMetadataRepository.save(metadata);
            logger.info("<<< Status da imagem {} atualizado para: {}", imageId, status);

            // Futuramente, aqui iremos chamar um serviço para notificar o usuário via WebSocket.

        } catch (Exception e) {
            logger.error("!!! Falha ao processar mensagem de resultado do Kafka: {}", message, e);
        }
    }
}