package br.com.pixpro.project_service.dto;

import jakarta.validation.constraints.NotBlank;

public record GenerateImageRequestDto(
        @NotBlank(message = "O prompt é obrigatório") String prompt,
        @NotBlank(message = "O modelo é obrigatório") String modelName
) {}