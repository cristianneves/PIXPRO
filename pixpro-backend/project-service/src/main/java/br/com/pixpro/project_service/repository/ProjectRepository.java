package br.com.pixpro.project_service.repository;

import br.com.pixpro.project_service.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
