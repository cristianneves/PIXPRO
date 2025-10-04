package br.com.pixpro.project_service.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProjectRequestDto(@NotBlank String name) {
}