package br.com.pixpro.project_service.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@Setter
@EqualsAndHashCode(exclude = {"images"})
@ToString(exclude = {"images"})
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Coluna para armazenar o ID do usuário que é dono deste projeto.
    // Esta é a ligação crucial entre o auth-service e o project-service.
    @Column(nullable = false)
    private Long userId;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "project", // "project" é o nome do campo na classe ImageMetadata
            cascade = CascadeType.ALL, // Se apagar o projeto, apaga as imagens junto
            orphanRemoval = true // Remove imagens da lista se elas não tiverem mais um projeto
    )
    @JsonManagedReference
    private List<ImageMetadata> images = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}