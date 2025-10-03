package br.com.pixpro.auth_service.controller;

import br.com.pixpro.auth_service.dto.LoginRequestDto;
import br.com.pixpro.auth_service.dto.LoginResponseDto;
import br.com.pixpro.auth_service.dto.RegisterRequestDto;
import br.com.pixpro.auth_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDto registerRequest) {
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
}
