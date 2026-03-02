package br.com.pixpro.project_service.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private StorageService storageService;

    @Test
    @DisplayName("Should upload file successfully and return storage key")
    void uploadFile_ValidFile_ReturnsStorageKey() throws IOException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("test data".getBytes()));
        when(mockFile.getSize()).thenReturn(9L);

        // Act
        String storageKey = storageService.uploadFile(mockFile);

        // Assert
        assertThat(storageKey).isNotNull();
        assertThat(storageKey).contains("test.jpg");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when file upload fails")
    void uploadFile_IoException_ThrowsRuntimeException() throws IOException {
        // Arrange
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test.jpg");
        when(mockFile.getInputStream()).thenThrow(new IOException("File read error"));

        // Act & Assert
        assertThatThrownBy(() -> storageService.uploadFile(mockFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao fazer upload do arquivo");
    }

    @Test
    @DisplayName("Should generate presigned URL successfully")
    void generatePresignedUrl_ValidObjectKey_ReturnsUrl() throws Exception {
        // Arrange
        String objectKey = "test-file.jpg";
        URI mockUri = URI.create("https://example.com/presigned-url");
        
        PresignedGetObjectRequest mockPresignedRequest = mock(PresignedGetObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(mockUri.toURL());
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenReturn(mockPresignedRequest);

        // Act
        String url = storageService.generatePresignedUrl(objectKey);

        // Assert
        assertThat(url).isEqualTo("https://example.com/presigned-url");
        verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));
    }
}
