package br.com.pixpro.auth_service.service;

import br.com.pixpro.auth_service.dto.LoginRequestDto;
import br.com.pixpro.auth_service.dto.UpdateUserRequestDto;
import br.com.pixpro.auth_service.exception.EmailAlreadyExistsException;
import br.com.pixpro.auth_service.exception.UserNotFoundException;
import br.com.pixpro.auth_service.model.Role;
import br.com.pixpro.auth_service.model.User;
import br.com.pixpro.auth_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(UserRepository userRepository,  PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public User registerUser(String name, String email, String password) {
        // 1. Verifica se o e-mail já está em uso
        if (userRepository.findByEmail(email).isPresent()) {
            // Lança uma exceção se o e-mail já existir. Trataremos essa exceção no Controller.
            throw new EmailAlreadyExistsException("Email já cadastrado.");
        }

        // 2. Cria uma nova instância do usuário
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);

        // 3. Criptografa a senha antes de salvar
        newUser.setPassword(passwordEncoder.encode(password));

        // 4. Define o papel padrão para novos usuários
        newUser.setRole(Role.ROLE_USER);

        // 5. Salva o novo usuário no banco e o retorna
        return userRepository.save(newUser);
    }

    public String login(LoginRequestDto loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));

        // Cria um mapa para adicionar informações extras
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("name", user.getName());

        return jwtService.generateToken(extraClaims, user);
    }

    // 1. Buscar dados do usuário atual
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado."));
    }

    // 2. Atualizar dados
    public User updateUser(Long userId, UpdateUserRequestDto request) {
        User user = getUserById(userId);

        // Atualiza o nome se foi enviado
        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }

        // Atualiza a senha se foi enviada (criptografando novamente)
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return userRepository.save(user);
    }

    // 3. Deletar conta
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

}
