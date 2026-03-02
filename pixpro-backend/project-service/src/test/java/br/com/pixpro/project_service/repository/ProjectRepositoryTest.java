package br.com.pixpro.project_service.repository;

import br.com.pixpro.project_service.model.Project;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should find all projects by user ID")
    void findAllByUserId_ValidUserId_ReturnsProjects() {
        // Arrange
        Long userId = 123L;
        
        Project project1 = new Project();
        project1.setName("Project 1");
        project1.setUserId(userId);
        entityManager.persist(project1);

        Project project2 = new Project();
        project2.setName("Project 2");
        project2.setUserId(userId);
        entityManager.persist(project2);

        Project project3 = new Project();
        project3.setName("Project 3");
        project3.setUserId(999L);
        entityManager.persist(project3);

        entityManager.flush();

        // Act
        List<Project> result = projectRepository.findAllByUserId(userId);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Project::getName).containsExactlyInAnyOrder("Project 1", "Project 2");
    }

    @Test
    @DisplayName("Should return empty list when no projects found for user ID")
    void findAllByUserId_NoProjects_ReturnsEmptyList() {
        // Arrange
        Long userId = 999L;

        // Act
        List<Project> result = projectRepository.findAllByUserId(userId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should save project with auto-generated ID and timestamps")
    void save_ValidProject_SavesSuccessfully() {
        // Arrange
        Project project = new Project();
        project.setName("Test Project");
        project.setUserId(123L);

        // Act
        Project saved = projectRepository.save(project);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Project");
        assertThat(saved.getUserId()).isEqualTo(123L);
    }
}
