package br.com.pixpro.project_service.controller;

import br.com.pixpro.project_service.dto.CreateProjectRequestDto;
import br.com.pixpro.project_service.model.AuthenticatedUser;
import br.com.pixpro.project_service.model.Project;
import br.com.pixpro.project_service.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

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
}