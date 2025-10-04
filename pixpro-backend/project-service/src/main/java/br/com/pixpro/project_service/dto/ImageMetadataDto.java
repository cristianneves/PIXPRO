package br.com.pixpro.project_service.dto;


import br.com.pixpro.project_service.model.ProcessingStatus;

import java.time.LocalDateTime;

public record ImageMetadataDto(
        Long id,
        String fileName,
        ProcessingStatus status,
        Long projectId,
        LocalDateTime createdAt
) {
}
