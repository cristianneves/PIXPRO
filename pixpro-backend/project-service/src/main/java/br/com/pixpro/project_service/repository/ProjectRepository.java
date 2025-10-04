package br.com.pixpro.project_service.repository;

import br.com.pixpro.project_service.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByUserId(Long userId);
}
