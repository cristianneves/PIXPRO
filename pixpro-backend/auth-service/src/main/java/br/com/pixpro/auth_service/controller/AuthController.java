package br.com.pixpro.auth_service.controller;

import br.com.pixpro.auth_service.dto.LoginRequestDto;
import br.com.pixpro.auth_service.dto.LoginResponseDto;
import br.com.pixpro.auth_service.dto.RegisterRequestDto;
import br.com.pixpro.auth_service.dto.UpdateUserRequestDto;
import br.com.pixpro.auth_service.model.User;
import br.com.pixpro.auth_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        authService.registerUser(
                registerRequest.name(),
                registerRequest.email(),
                registerRequest.password()
        );
        return new ResponseEntity<>("Usuário registrado com sucesso!", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequest) {
        String token = authService.login(loginRequest);

        // Se o login for bem-sucedido, retorna o token no corpo da resposta.
        return ResponseEntity.ok(new LoginResponseDto(token));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal User user) {
        // O Spring Security injeta automaticamente o usuário logado aqui
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateUser(@AuthenticationPrincipal User user,
                                           @RequestBody UpdateUserRequestDto request) {
        // Usamos o ID do token (seguro) para garantir que ele só altera a si mesmo
        User updatedUser = authService.updateUser(user.getId(), request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal User user) {
        authService.deleteUser(user.getId());
        return ResponseEntity.noContent().build();
    }
}
