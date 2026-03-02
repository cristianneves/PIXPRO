package br.com.pixpro.project_service.controller;

import br.com.pixpro.project_service.model.Project;
import br.com.pixpro.project_service.repository.ImageMetadataRepository;
import br.com.pixpro.project_service.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/admin")
public class AdminController {

    private final ProjectRepository projectRepository;
    private final ImageMetadataRepository imageMetadataRepository;

    public AdminController(ProjectRepository projectRepository, ImageMetadataRepository imageMetadataRepository) {
        this.projectRepository = projectRepository;
        this.imageMetadataRepository = imageMetadataRepository;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getProjectStats() {
        long totalProjects = projectRepository.count();
        long totalImages = imageMetadataRepository.count();


        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProjects", totalProjects);
        stats.put("totalImagesProcessed", totalImages);

        return ResponseEntity.ok(stats);
    }

    // 1. Listar TODOS os Projetos do Sistema (Visão Global)
    @GetMapping("/projects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Project>> getAllProjectsGlobal(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Project> allProjects = projectRepository.findAll(pageable);

        return ResponseEntity.ok(allProjects);
    }

    // 2. Deletar Qualquer Projeto (Moderação)
    // Diferente do delete normal, este não verifica se o userId bate.
    @DeleteMapping("/projects/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> forceDeleteProject(@PathVariable Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            return ResponseEntity.notFound().build();
        }
        // O CascadeType.ALL na entidade Project vai apagar as imagens e metadados junto.
        projectRepository.deleteById(projectId);

        return ResponseEntity.noContent().build();
    }
}