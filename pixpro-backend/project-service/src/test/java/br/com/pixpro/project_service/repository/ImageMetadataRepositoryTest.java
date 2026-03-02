package br.com.pixpro.project_service.repository;

import br.com.pixpro.project_service.model.ImageMetadata;
import br.com.pixpro.project_service.model.ProcessingStatus;
import br.com.pixpro.project_service.model.Project;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ImageMetadataRepositoryTest {

    @Autowired
    private ImageMetadataRepository imageMetadataRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should save image metadata with auto-generated ID and timestamps")
    void save_ValidImageMetadata_SavesSuccessfully() {
        // Arrange
        Project project = new Project();
        project.setName("Test Project");
        project.setUserId(123L);
        entityManager.persist(project);

        ImageMetadata metadata = new ImageMetadata();
        metadata.setFileName("test.jpg");
        metadata.setProject(project);
        metadata.setStatus(ProcessingStatus.UPLOAD_PENDING);
        metadata.setOriginalStoragePath("path/to/original.jpg");

        // Act
        ImageMetadata saved = imageMetadataRepository.save(metadata);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getFileName()).isEqualTo("test.jpg");
        assertThat(saved.getStatus()).isEqualTo(ProcessingStatus.UPLOAD_PENDING);
    }

    @Test
    @DisplayName("Should find image metadata by ID")
    void findById_ExistingId_ReturnsImageMetadata() {
        // Arrange
        Project project = new Project();
        project.setName("Test Project");
        project.setUserId(123L);
        entityManager.persist(project);

        ImageMetadata metadata = new ImageMetadata();
        metadata.setFileName("test.jpg");
        metadata.setProject(project);
        metadata.setStatus(ProcessingStatus.UPLOAD_PENDING);
        metadata.setOriginalStoragePath("path/to/original.jpg");
        entityManager.persist(metadata);
        entityManager.flush();

        // Act
        Optional<ImageMetadata> result = imageMetadataRepository.findById(metadata.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getFileName()).isEqualTo("test.jpg");
    }

    @Test
    @DisplayName("Should return empty when image metadata not found")
    void findById_NonExistingId_ReturnsEmpty() {
        // Act
        Optional<ImageMetadata> result = imageMetadataRepository.findById(999L);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should delete image metadata")
    void delete_ExistingImageMetadata_DeletesSuccessfully() {
        // Arrange
        Project project = new Project();
        project.setName("Test Project");
        project.setUserId(123L);
        entityManager.persist(project);

        ImageMetadata metadata = new ImageMetadata();
        metadata.setFileName("test.jpg");
        metadata.setProject(project);
        metadata.setStatus(ProcessingStatus.UPLOAD_PENDING);
        metadata.setOriginalStoragePath("path/to/original.jpg");
        entityManager.persist(metadata);
        entityManager.flush();

        Long metadataId = metadata.getId();

        // Act
        imageMetadataRepository.delete(metadata);
        entityManager.flush();

        // Assert
        Optional<ImageMetadata> result = imageMetadataRepository.findById(metadataId);
        assertThat(result).isEmpty();
    }
}
