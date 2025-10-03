package br.com.pixpro.project_service.repository;

import br.com.pixpro.project_service.model.ImageMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageMetadataRepository extends JpaRepository<ImageMetadata, Long> {
}
