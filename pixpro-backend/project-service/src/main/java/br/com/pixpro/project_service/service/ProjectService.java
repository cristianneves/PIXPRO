package br.com.pixpro.project_service.service;

import br.com.pixpro.project_service.dto.CreateProjectRequestDto;
import br.com.pixpro.project_service.exception.ProjectNotFoundException;
import br.com.pixpro.project_service.model.Project;
import br.com.pixpro.project_service.repository.ImageMetadataRepository;
import br.com.pixpro.project_service.repository.ProjectRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ImageMetadataRepository imageMetadataRepository;

    public ProjectService(ProjectRepository projectRepository, ImageMetadataRepository imageMetadataRepository) {
        this.projectRepository = projectRepository;
        this.imageMetadataRepository = imageMetadataRepository;
    }


    /**
     * Cria um novo projeto para um usuário específico.
     * @param requestDto DTO contendo o nome do projeto.
     * @param userId ID do usuário autenticado que será o dono do projeto.
     * @return A entidade Project que foi salva no banco de dados.
     */
    @Transactional // Garante que a operação seja atômica (ou tudo funciona, ou nada é salvo)
    public Project createProject(CreateProjectRequestDto requestDto, Long userId) {
        // Validação (opcional, mas boa prática)
        if (userId == null) {
            // Em um cenário real, poderíamos ter uma exceção personalizada aqui
            throw new IllegalArgumentException("O ID do usuário não pode ser nulo.");
        }

        // Cria a nova entidade Project
        Project newProject = new Project();
        newProject.setName(requestDto.name());
        newProject.setUserId(userId); // Associa o projeto ao usuário

        // Salva a entidade no banco de dados usando o repositório e a retorna
        return projectRepository.save(newProject);
    }

    /**
     * Busca todos os projetos pertencentes a um usuário.
     * @param userId O ID do usuário autenticado.
     * @return Uma lista de projetos.
     */
    @Transactional(readOnly = true) // Otimização para operações de apenas leitura
    public List<Project> findProjectsByUserId(Long userId) {
        return projectRepository.findAllByUserId(userId);
    }

    /**
     * Busca um único projeto pelo seu ID, garantindo que ele pertença ao usuário.
     * @param projectId O ID do projeto a ser buscado.
     * @param userId O ID do usuário autenticado.
     * @return A entidade Project.
     */
    @Transactional(readOnly = true)
    public Project findProjectById(Long projectId, Long userId) {
        // 1. Busca o projeto no banco
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Projeto com ID " + projectId + " não encontrado."));

        // 2. REGRA DE SEGURANÇA: Verifica se o ID do dono do projeto é o mesmo do usuário logado
        if (!project.getUserId().equals(userId)) {
            // Se não for, lança uma exceção de acesso negado.
            throw new AccessDeniedException("Você não tem permissão para acessar este projeto.");
        }

        // 3. Se tudo estiver certo, retorna o projeto
        return project;
    }
}
