package br.com.pixpro.auth_service.controller;

import br.com.pixpro.auth_service.model.User;
import br.com.pixpro.auth_service.repository.UserRepository;
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
@RequestMapping("/api/auth/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')") // Garante que só admins acessem
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        long totalUsers = userRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);

        return ResponseEntity.ok(stats);
    }

    // 1. Listar Usuários (Paginado)
    // Exemplo: GET /api/admin/users?page=0&size=10
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Cria a paginação ordenada por ID decrescente (mais novos primeiro)
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> usersPage = userRepository.findAll(pageable);

        return ResponseEntity.ok(usersPage);
    }

    // 2. Banir Usuário (Deletar conta específica)
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> banUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}