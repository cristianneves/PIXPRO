package br.com.pixpro.project_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class MinioConfig {

    @Value("${aws.s3.endpoint}")
    private String endpoint; // Usado pelo S3Client (interno)

    @Value("${aws.s3.public-endpoint}") // <-- NOVO VALOR
    private String publicEndpoint; // Usado pelo S3Presigner (externo)

    @Value("${aws.s3.access-key-id}")
    private String accessKey;

    @Value("${aws.s3.secret-access-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        // O S3Client continua a usar o endpoint INTERNO
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint)) // <- Sem alterações
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .forcePathStyle(true)
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        S3Configuration s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        // O S3Presigner DEVE usar o endpoint EXTERNO
        return S3Presigner.builder()
                .endpointOverride(URI.create(publicEndpoint)) // <-- MUDANÇA AQUI
                .region(Region.of(region))
                .serviceConfiguration(s3Configuration)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}