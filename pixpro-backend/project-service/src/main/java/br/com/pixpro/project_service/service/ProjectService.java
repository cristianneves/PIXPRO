package br.com.pixpro.project_service.service;

import br.com.pixpro.project_service.dto.CreateProjectRequestDto;
import br.com.pixpro.project_service.model.Project;
import br.com.pixpro.project_service.repository.ImageMetadataRepository;
import br.com.pixpro.project_service.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
