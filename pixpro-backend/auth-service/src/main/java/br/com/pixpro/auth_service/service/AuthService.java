package br.com.pixpro.auth_service.service;

import br.com.pixpro.auth_service.dto.LoginRequestDto;
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
        // 1. O AuthenticationManager usa as credenciais para tentar autenticar o usuário.
        // Ele buscará o usuário no banco e comparará as senhas usando o PasswordEncoder.
        // Se as credenciais forem inválidas, ele lançará uma exceção (que será tratada pelo nosso GlobalExceptionHandler).
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        // 2. Se a autenticação for bem-sucedida, buscamos os detalhes do usuário.
        // Lançamos uma exceção se, por algum motivo, o usuário não for encontrado após a autenticação.
        UserDetails user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado após autenticação."));

        // 3. Geramos o token JWT para o usuário autenticado.
        return jwtService.generateToken(user);
    }

}
