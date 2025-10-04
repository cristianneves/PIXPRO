package br.com.pixpro.project_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class StorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Faz o upload de um arquivo para o MinIO.
     * @param file O arquivo enviado pelo usuário.
     * @return A chave (nome único) do arquivo salvo no bucket.
     */
    public String uploadFile(MultipartFile file) {
        try {
            // 1. Gera um nome de arquivo único para evitar colisões de nomes.
            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // 2. Cria a requisição de upload para o S3/MinIO.
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueFileName)
                    .build();

            // 3. Envia o arquivo.
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 4. Retorna o nome único do arquivo para ser salvo no banco de dados.
            return uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("Falha ao fazer upload do arquivo.", e);
        }
    }
}