package br.com.pixpro.project_service.controller;

import br.com.pixpro.project_service.dto.CreateProjectRequestDto;
import br.com.pixpro.project_service.dto.GenerateImageRequestDto;
import br.com.pixpro.project_service.dto.ImageMetadataDto;
import br.com.pixpro.project_service.dto.UpdateProjectRequestDto;
import br.com.pixpro.project_service.model.AuthenticatedUser;
import br.com.pixpro.project_service.model.Project;
import br.com.pixpro.project_service.service.ProjectService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);


    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@Valid @RequestBody CreateProjectRequestDto requestDto,
                                                 Authentication authentication) {
        // 1. Pega o "principal" do objeto de autenticação, que é nosso AuthenticatedUser.
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();

        // 2. Extrai o ID real do usuário que está logado.
        Long userId = authenticatedUser.getId();

        // 3. Chama o serviço com o ID dinâmico.
        Project createdProject = projectService.createProject(requestDto, userId);

        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Project>> getUserProjects(Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        List<Project> projects = projectService.findProjectsByUserId(user.getId());

        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id, Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        Project project = projectService.findProjectById(id, user.getId());

        return ResponseEntity.ok(project);
    }

    @GetMapping("/{projectId}/images/{imageId}/download-url")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable Long projectId,
                                                              @PathVariable Long imageId,
                                                              Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        String url = projectService.generateDownloadUrlForImage(projectId, imageId, user.getId());

        // Retorna a URL dentro de um objeto JSON para uma resposta mais limpa.
        Map<String, String> response = Map.of("downloadUrl", url);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{projectId}/images")
    public ResponseEntity<List<ImageMetadataDto>> addImagesToProject(
            @PathVariable Long projectId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("prompt") String prompt,
            @RequestParam("modelName") String modelName,
            Authentication authentication) {

        logger.info(">>> Recebida requisição para adicionar {} imagens ao projeto {}", files.size(), projectId);

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        Long userId = user.getId();
        logger.info(">>> Usuário autenticado com ID: {}", userId);

        logger.info(">>> Chamando o serviço ProjectService com o prompt: [{}]", prompt);

        // Passe os novos parâmetros para o serviço
        List<ImageMetadataDto> createdMetadataDto = projectService.addImagesToProject(
                projectId,
                userId,
                files,
                prompt,
                modelName
        );
        logger.info(">>> Serviço executado com sucesso. Retornando {} metadados.", createdMetadataDto.size());

        return new ResponseEntity<>(createdMetadataDto, HttpStatus.CREATED);
    }

    // Novo endpoint para Geração (Text-to-Image)
    @PostMapping("/{projectId}/generate")
    public ResponseEntity<ImageMetadataDto> generateImage(
            @PathVariable Long projectId,
            @Valid @RequestBody GenerateImageRequestDto requestDto,
            Authentication authentication) {

        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();

        logger.info(">>> Requisição de Geração de Imagem: {}", requestDto.prompt());

        ImageMetadataDto result = projectService.generateImageFromText(projectId, user.getId(), requestDto);

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateProjectRequestDto requestDto,
                                                 Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        Project updatedProject = projectService.updateProject(id, user.getId(), requestDto);

        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id, Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        projectService.deleteProject(id, user.getId());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{projectId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long projectId,
                                            @PathVariable Long imageId,
                                            Authentication authentication) {
        AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
        projectService.deleteImageFromProject(projectId, imageId, user.getId());

        return ResponseEntity.noContent().build();
    }
}