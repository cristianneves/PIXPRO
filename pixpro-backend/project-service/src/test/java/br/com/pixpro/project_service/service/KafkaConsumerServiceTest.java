package br.com.pixpro.project_service.service;

import br.com.pixpro.project_service.model.ImageMetadata;
import br.com.pixpro.project_service.model.ProcessingStatus;
import br.com.pixpro.project_service.repository.ImageMetadataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {

    @Mock
    private ImageMetadataRepository imageMetadataRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    @Test
    @DisplayName("Should process valid Kafka message and update image metadata")
    @SuppressWarnings("unchecked")
    void consumeImageProcessingResult_ValidMessage_UpdatesMetadata() throws Exception {
        // Arrange
        String message = "{\"imageId\":123,\"status\":\"COMPLETED\",\"processedStoragePath\":\"path/to/processed.jpg\"}";
        
        ImageMetadata metadata = new ImageMetadata();
        metadata.setId(123L);
        metadata.setStatus(ProcessingStatus.UPLOAD_PENDING);

        when(objectMapper.readValue(eq(message), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(java.util.Map.of(
                        "imageId", 123,
                        "status", "COMPLETED",
                        "processedStoragePath", "path/to/processed.jpg"
                ));
        when(imageMetadataRepository.findById(123L)).thenReturn(Optional.of(metadata));

        // Act
        kafkaConsumerService.consumeImageProcessingResult(message);

        // Assert
        verify(imageMetadataRepository).findById(123L);
        verify(imageMetadataRepository).save(metadata);
        assert metadata.getStatus() == ProcessingStatus.COMPLETED;
        assert metadata.getProcessedStoragePath().equals("path/to/processed.jpg");
    }

    @Test
    @DisplayName("Should handle invalid JSON gracefully")
    @SuppressWarnings("unchecked")
    void consumeImageProcessingResult_InvalidJson_DoesNotThrow() throws Exception {
        // Arrange
        String invalidMessage = "{invalid-json}";
        
        when(objectMapper.readValue(eq(invalidMessage), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenThrow(new com.fasterxml.jackson.core.JsonParseException(null, "Invalid JSON"));

        // Act & Assert - should not throw exception
        kafkaConsumerService.consumeImageProcessingResult(invalidMessage);
        
        // Verify that save was never called due to exception
        verify(imageMetadataRepository, never()).save(any());
    }
}
