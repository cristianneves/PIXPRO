//package br.com.pixpro.project_service.service;
//
//import br.com.pixpro.project_service.dto.CreateProjectRequestDto;
//import br.com.pixpro.project_service.dto.ImageMetadataDto;
//import br.com.pixpro.project_service.dto.UpdateProjectRequestDto;
//import br.com.pixpro.project_service.exception.ImageNotFoundException;
//import br.com.pixpro.project_service.exception.ProjectNotFoundException;
//import br.com.pixpro.project_service.model.ImageMetadata;
//import br.com.pixpro.project_service.model.ProcessingStatus;
//import br.com.pixpro.project_service.model.Project;
//import br.com.pixpro.project_service.repository.ImageMetadataRepository;
//import br.com.pixpro.project_service.repository.ProjectRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ProjectServiceTest {
//
//    @Mock
//    private ProjectRepository projectRepository;
//
//    @Mock
//    private ImageMetadataRepository imageMetadataRepository;
//
//    @Mock
//    private KafkaProducerService kafkaProducerService;
//
//    @Mock
//    private ObjectMapper objectMapper;
//
//    @Mock
//    private StorageService storageService;
//
//    @InjectMocks
//    private ProjectService projectService;
//
//    @Test
//    @DisplayName("Should create project successfully")
//    void createProject_ValidRequest_CreatesProject() {
//        // Arrange
//        CreateProjectRequestDto requestDto = new CreateProjectRequestDto("Test Project");
//        Long userId = 123L;
//
//        Project expectedProject = new Project();
//        expectedProject.setId(1L);
//        expectedProject.setName("Test Project");
//        expectedProject.setUserId(userId);
//
//        when(projectRepository.save(any(Project.class))).thenReturn(expectedProject);
//
//        // Act
//        Project result = projectService.createProject(requestDto, userId);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.getName()).isEqualTo("Test Project");
//        assertThat(result.getUserId()).isEqualTo(userId);
//        verify(projectRepository).save(any(Project.class));
//    }
//
//    @Test
//    @DisplayName("Should throw exception when userId is null")
//    void createProject_NullUserId_ThrowsException() {
//        // Arrange
//        CreateProjectRequestDto requestDto = new CreateProjectRequestDto("Test Project");
//
//        // Act & Assert
//        assertThatThrownBy(() -> projectService.createProject(requestDto, null))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("O ID do usuário não pode ser nulo");
//    }
//
//    @Test
//    @DisplayName("Should find projects by user ID")
//    void findProjectsByUserId_ValidUserId_ReturnsProjects() {
//        // Arrange
//        Long userId = 123L;
//        Project project1 = new Project();
//        project1.setId(1L);
//        project1.setUserId(userId);
//
//        Project project2 = new Project();
//        project2.setId(2L);
//        project2.setUserId(userId);
//
//        when(projectRepository.findAllByUserId(userId)).thenReturn(List.of(project1, project2));
//
//        // Act
//        List<Project> result = projectService.findProjectsByUserId(userId);
//
//        // Assert
//        assertThat(result).hasSize(2);
//        verify(projectRepository).findAllByUserId(userId);
//    }
//
//    @Test
//    @DisplayName("Should find project by ID when user is owner")
//    void findProjectById_UserIsOwner_ReturnsProject() {
//        // Arrange
//        Long projectId = 1L;
//        Long userId = 123L;
//
//        Project project = new Project();
//        project.setId(projectId);
//        project.setUserId(userId);
//        project.setName("Test Project");
//
//        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
//
//        // Act
//        Project result = projectService.findProjectById(projectId, userId);
//
//        // Assert
//        assertThat(result).isNotNull();
//        assertThat(result.getId()).isEqualTo(projectId);
//        verify(projectRepository).findById(projectId);
//    }
//
//    @Test
//    @DisplayName("Should throw exception when project not found")
//    void findProjectById_ProjectNotFound_ThrowsException() {
//        // Arrange
//        Long projectId = 1L;
//        Long userId = 123L;
//
//        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> projectService.findProjectById(projectId, userId))
//                .isInstanceOf(ProjectNotFoundException.class)
//                .hasMessageContaining("Projeto com ID " + projectId + " não encontrado");
//    }
//
//    @Test
//    @DisplayName("Should throw exception when user is not project owner")
//    void findProjectById_UserNotOwner_ThrowsException() {
//        // Arrange
//        Long projectId = 1L;
//        Long ownerId = 123L;
//        Long differentUserId = 999L;
//
//        Project project = new Project();
//        project.setId(projectId);
//        project.setUserId(ownerId);
//
//        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
//
//        // Act & Assert
//        assertThatThrownBy(() -> projectService.findProjectById(projectId, differentUserId))
//                .isInstanceOf(AccessDeniedException.class)
//                .hasMessageContaining("Você não tem permissão para acessar este projeto");
//    }
//
//    @Test
//    @DisplayName("Should add images to project successfully")
//    void addImagesToProject_ValidFiles_CreatesMetadata() throws Exception {
//        // Arrange
//        Long projectId = 1L;
//        Long userId = 123L;
//
//        Project project = new Project();
//        project.setId(projectId);
//        project.setUserId(userId);
//
//        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
//        List<MultipartFile> files = List.of(file);
//
//        ImageMetadata savedMetadata = new ImageMetadata();
//        savedMetadata.setId(1L);
//        savedMetadata.setFileName("test.jpg");
//        savedMetadata.setProject(project);
//        savedMetadata.setStatus(ProcessingStatus.UPLOAD_PENDING);
//        savedMetadata.setOriginalStoragePath("storage-key");
//
//        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
//        when(storageService.uploadFile(any(MultipartFile.class))).thenReturn("storage-key");
//        when(imageMetadataRepository.saveAll(anyList())).thenReturn(List.of(savedMetadata));
//        when(objectMapper.writeValueAsString(any())).thenReturn("{\"imageId\":1}");
//
//        // Act
//        List<ImageMetadataDto> result = projectService.addImagesToProject(projectId, userId, files);
//
//        // Assert
//        assertThat(result).hasSize(1);
//        assertThat(result.get(0).fileName()).isEqualTo("test.jpg");
//        verify(storageService).uploadFile(any(MultipartFile.class));
//        verify(imageMetadataRepository).saveAll(anyList());
//        verify(kafkaProducerService).sendImageProcessingRequest(anyString(), anyString());
//    }
//
//    @Test
//    @DisplayName("Should update project name successfully")
//    void updateProject_ValidRequest_UpdatesProject() {
//        // Arrange
//        Long projectId = 1L;
//        Long userId = 123L;
//        UpdateProjectRequestDto requestDto = new UpdateProjectRequestDto("Updated Name");
//
//        Project project = new Project();
//        project.setId(projectId);
//        project.setUserId(userId);
//        project.setName("Old Name");
//
//        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
//
//        // Act
//        Project result = projectService.updateProject(projectId, userId, requestDto);
//
//        // Assert
//        assertThat(result.getName()).isEqualTo("Updated Name");
//        verify(projectRepository).findById(projectId);
//    }
//
//    @Test
//    @DisplayName("Should delete project successfully")
//    void deleteProject_ValidProjectAndUser_DeletesProject() {
//        // Arrange
//        Long projectId = 1L;
//        Long userId = 123L;
//
//        Project project = new Project();
//        project.setId(projectId);
//        project.setUserId(userId);
//
//        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
//
//        // Act
//        projectService.deleteProject(projectId, userId);
//
//        // Assert
//        verify(projectRepository).findById(projectId);
//        verify(projectRepository).delete(project);
//    }
//
//    @Test
//    @DisplayName("Should delete image from project successfully")
//    void deleteImageFromProject_ValidImage_DeletesImage() {
//        // Arrange
//        Long projectId = 1L;
//        Long imageId = 1L;
//        Long userId = 123L;
//
//        Project project = new Project();
//        project.setId(projectId);
//        project.setUserId(userId);
//
//        ImageMetadata metadata = new ImageMetadata();
//        metadata.setId(imageId);
//        metadata.setProject(project);
//
//        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
//        when(imageMetadataRepository.findById(imageId)).thenReturn(Optional.of(metadata));
//
//        // Act
//        projectService.deleteImageFromProject(projectId, imageId, userId);
//
//        // Assert
//        verify(projectRepository).findById(projectId);
//        verify(imageMetadataRepository).findById(imageId);
//        verify(imageMetadataRepository).delete(metadata);
//    }
//
//    @Test
//    @DisplayName("Should throw exception when deleting non-existent image")
//    void deleteImageFromProject_ImageNotFound_ThrowsException() {
//        // Arrange
//        Long projectId = 1L;
//        Long imageId = 999L;
//        Long userId = 123L;
//
//        Project project = new Project();
//        project.setId(projectId);
//        project.setUserId(userId);
//
//        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
//        when(imageMetadataRepository.findById(imageId)).thenReturn(Optional.empty());
//
//        // Act & Assert
//        assertThatThrownBy(() -> projectService.deleteImageFromProject(projectId, imageId, userId))
//                .isInstanceOf(ImageNotFoundException.class)
//                .hasMessageContaining("Imagem com ID " + imageId + " não encontrada");
//    }
//
//    @Test
//    @DisplayName("Should generate download URL for processed image")
//    void generateDownloadUrlForImage_ProcessedImage_ReturnsUrl() {
//        // Arrange
//        Long projectId = 1L;
//        Long imageId = 1L;
//        Long userId = 123L;
//
//        Project project = new Project();
//        project.setId(projectId);
//        project.setUserId(userId);
//
//        ImageMetadata metadata = new ImageMetadata();
//        metadata.setId(imageId);
//        metadata.setProject(project);
//        metadata.setStatus(ProcessingStatus.COMPLETED);
//        metadata.setProcessedStoragePath("processed-path");
//
//        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
//        when(imageMetadataRepository.findById(imageId)).thenReturn(Optional.of(metadata));
//        when(storageService.generatePresignedUrl("processed-path")).thenReturn("https://example.com/download");
//
//        // Act
//        String result = projectService.generateDownloadUrlForImage(projectId, imageId, userId);
//
//        // Assert
//        assertThat(result).isEqualTo("https://example.com/download");
//        verify(storageService).generatePresignedUrl("processed-path");
//    }
//
//    @Test
//    @DisplayName("Should throw exception when image not yet processed")
//    void generateDownloadUrlForImage_UnprocessedImage_ThrowsException() {
//        // Arrange
//        Long projectId = 1L;
//        Long imageId = 1L;
//        Long userId = 123L;
//
//        Project project = new Project();
//        project.setId(projectId);
//        project.setUserId(userId);
//
//        ImageMetadata metadata = new ImageMetadata();
//        metadata.setId(imageId);
//        metadata.setProject(project);
//        metadata.setStatus(ProcessingStatus.UPLOAD_PENDING);
//
//        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
//        when(imageMetadataRepository.findById(imageId)).thenReturn(Optional.of(metadata));
//
//        // Act & Assert
//        assertThatThrownBy(() -> projectService.generateDownloadUrlForImage(projectId, imageId, userId))
//                .isInstanceOf(IllegalStateException.class)
//                .hasMessageContaining("O processamento da imagem ainda não foi concluído");
//    }
//
//    @SuppressWarnings("unchecked")
//    private static <T> List<T> anyList() {
//        return any(List.class);
//    }
//}
