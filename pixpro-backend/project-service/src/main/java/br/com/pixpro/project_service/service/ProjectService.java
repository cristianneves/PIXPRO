package br.com.pixpro.project_service.service;

import br.com.pixpro.project_service.dto.CreateProjectRequestDto;
import br.com.pixpro.project_service.dto.ImageMetadataDto;
import br.com.pixpro.project_service.dto.UpdateProjectRequestDto;
import br.com.pixpro.project_service.exception.ImageNotFoundException;
import br.com.pixpro.project_service.exception.ProjectNotFoundException;
import br.com.pixpro.project_service.model.ImageMetadata;
import br.com.pixpro.project_service.model.ProcessingStatus;
import br.com.pixpro.project_service.model.Project;
import br.com.pixpro.project_service.repository.ImageMetadataRepository;
import br.com.pixpro.project_service.repository.ProjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ImageMetadataRepository imageMetadataRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);
    private final StorageService storageService;



    public ProjectService(ProjectRepository projectRepository, ImageMetadataRepository imageMetadataRepository,
                          KafkaProducerService kafkaProducerService, ObjectMapper objectMapper, StorageService storageService) {
        this.projectRepository = projectRepository;
        this.imageMetadataRepository = imageMetadataRepository;
        this.kafkaProducerService = kafkaProducerService;
        this.objectMapper = objectMapper;
        this.storageService = storageService;
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

    /**
     * Adiciona múltiplas imagens a um projeto existente, criando os metadados para cada uma.
     * @param projectId O ID do projeto onde as imagens serão adicionadas.
     * @param userId O ID do usuário autenticado, para verificação de permissão.
     * @param files A lista de arquivos de imagem enviados.
     * @return Uma lista com os metadados das imagens que foram criadas.
     */
    @Transactional
    public List<ImageMetadataDto> addImagesToProject(Long projectId, Long userId, List<MultipartFile> files) {
        Project project = findProjectById(projectId, userId);

        List<ImageMetadata> newImagesMetadata = files.stream().map(file -> {
            // A. Primeiro, faz o upload do arquivo para o MinIO e obtém sua chave única.
            String storageKey = storageService.uploadFile(file);
            logger.info(">>> Arquivo {} salvo no MinIO com a chave: {}", file.getOriginalFilename(), storageKey);

            // B. Cria a entidade de metadados com o caminho real.
            ImageMetadata metadata = new ImageMetadata();
            metadata.setFileName(file.getOriginalFilename());
            metadata.setProject(project);
            metadata.setStatus(ProcessingStatus.UPLOAD_PENDING);
            metadata.setOriginalStoragePath(storageKey); // Salva o caminho real retornado pelo MinIO

            return metadata;
        }).collect(Collectors.toList());

        // Salva todos os metadados no banco
        List<ImageMetadata> savedMetadata = imageMetadataRepository.saveAll(newImagesMetadata);

        // Envia uma mensagem para o Kafka para cada imagem salva
        savedMetadata.forEach(metadata -> {
            var kafkaMessage = Map.of(
                    "imageId", metadata.getId(),
                    "userId", userId,
                    "originalStoragePath", metadata.getOriginalStoragePath()
            );

            try {
                kafkaProducerService.sendImageProcessingRequest(
                        "image-processing-queue",
                        objectMapper.writeValueAsString(kafkaMessage)
                );
            } catch (Exception e) {
                logger.error("!!! Falha ao enviar mensagem para o Kafka para imageId {}", metadata.getId(), e);
            }
        });

        // Mapeia para o DTO de resposta da API
        return savedMetadata.stream()
                .map(metadata -> new ImageMetadataDto(
                        metadata.getId(),
                        metadata.getFileName(),
                        metadata.getStatus(),
                        metadata.getProject().getId(),
                        metadata.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Atualiza o nome de um projeto existente.
     * @param projectId O ID do projeto a ser atualizado.
     * @param userId O ID do usuário autenticado para verificação de permissão.
     * @param requestDto DTO com as novas informações.
     * @return O projeto atualizado.
     */
    @Transactional
    public Project updateProject(Long projectId, Long userId, UpdateProjectRequestDto requestDto) {
        Project projectToUpdate = findProjectById(projectId, userId);
        projectToUpdate.setName(requestDto.name());

        return projectToUpdate;
    }

    /**
     * Deleta um projeto.
     * @param projectId O ID do projeto a ser deletado.
     * @param userId O ID do usuário autenticado para verificação de permissão.
     */
    @Transactional
    public void deleteProject(Long projectId, Long userId) {
        Project projectToDelete = findProjectById(projectId, userId);

        projectRepository.delete(projectToDelete);
    }

    /**
     * Deleta os metadados de uma imagem de um projeto.
     * @param projectId O ID do projeto ao qual a imagem pertence.
     * @param imageId O ID da imagem a ser deletada.
     * @param userId O ID do usuário autenticado para verificação de permissão.
     */
    @Transactional
    public void deleteImageFromProject(Long projectId, Long imageId, Long userId) {
        // 1. Primeiro, garante que o usuário tem acesso ao projeto.
        findProjectById(projectId, userId);

        // 2. Busca os metadados da imagem no banco de dados.
        ImageMetadata imageToDelete = imageMetadataRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Imagem com ID " + imageId + " não encontrada."));

        // 3. (Verificação extra de consistência) Garante que a imagem encontrada realmente pertence ao projeto informado.
        if (!imageToDelete.getProject().getId().equals(projectId)) {
            throw new AccessDeniedException("A imagem não pertence ao projeto especificado.");
        }

        // 4. Deleta os metadados da imagem.
        imageMetadataRepository.delete(imageToDelete);

        // Futuramente, aqui também seria o local para disparar um evento para deletar
        // o arquivo físico do armazenamento de objetos (MinIO/S3).
    }
}
