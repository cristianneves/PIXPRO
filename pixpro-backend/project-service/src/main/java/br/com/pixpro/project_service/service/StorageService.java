package br.com.pixpro.project_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
public class StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public StorageService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    private void ensureBucketExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (S3Exception e) {
            // Se o código for 404 (NoSuchBucket), criamos
            if (e.statusCode() == 404) {
                try {
                    s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
                    System.out.println(">>> Bucket " + bucketName + " criado automaticamente.");
                } catch (S3Exception createEx) {
                    throw new RuntimeException("Falha ao criar o bucket no MinIO.", createEx);
                }
            } else {
                throw e; // Outro erro (ex: permissão)
            }
        }
    }

    /**
     * Faz o upload de um arquivo para o MinIO.
     * @param file O arquivo enviado pelo usuário.
     * @return A chave (nome único) do arquivo salvo no bucket.
     */
    public String uploadFile(MultipartFile file) {
        // 1. GARANTE QUE O BUCKET EXISTE ANTES DE TENTAR O UPLOAD
        ensureBucketExists();

        try {
            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueFileName)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("Falha ao fazer upload do arquivo.", e);
        }
    }

    public String generatePresignedUrl(String objectKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(1000))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(presignRequest);
        return presignedGetObjectRequest.url().toString();
    }
}