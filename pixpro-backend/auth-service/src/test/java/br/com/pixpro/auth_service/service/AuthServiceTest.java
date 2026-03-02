package br.com.pixpro.auth_service.service;

import br.com.pixpro.auth_service.dto.LoginRequestDto;
import br.com.pixpro.auth_service.exception.EmailAlreadyExistsException;
import br.com.pixpro.auth_service.exception.UserNotFoundException;
import br.com.pixpro.auth_service.model.Role;
import br.com.pixpro.auth_service.model.User;
import br.com.pixpro.auth_service.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("registerUser creates user with encoded password and default role")
    void registerUser_Success() {
        // arrange
        String name = "Bob";
        String email = "bob@example.com";
        String rawPassword = "123456";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        // act
        User saved = authService.registerUser(name, email, rawPassword);

        // assert
        assertNotNull(saved.getId());
        assertEquals(name, saved.getName());
        assertEquals(email, saved.getEmail());
        assertEquals("encoded", saved.getPassword());
        assertEquals(Role.ROLE_USER, saved.getRole());

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser throws when email already exists")
    void registerUser_DuplicateEmail_Throws() {
        // arrange
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(new User()));

        // act + assert
        assertThrows(EmailAlreadyExistsException.class,
                () -> authService.registerUser("John", "taken@example.com", "pwd"));

        verify(userRepository).findByEmail("taken@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login returns JWT when credentials valid")
    void login_Success_ReturnsToken() {
        // arrange
        String email = "eve@example.com";
        String pwd = "pwd";
        LoginRequestDto req = new LoginRequestDto(email, pwd);

        Authentication auth = new UsernamePasswordAuthenticationToken(email, pwd, List.of());
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        User u = new User();
        u.setId(42L);
        u.setEmail(email);
        u.setPassword("encoded");
        u.setRole(Role.ROLE_USER);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(u));
        when(jwtService.generateToken(u)).thenReturn("jwt-token");

        // act
        String token = authService.login(req);

        // assert
        assertEquals("jwt-token", token);
        verify(authenticationManager).authenticate(any());
        verify(userRepository).findByEmail(email);
        verify(jwtService).generateToken(u);
    }

    @Test
    @DisplayName("login throws when user not found after authentication")
    void login_UserNotFound_Throws() {
        // arrange
        String email = "nouser@example.com";
        LoginRequestDto req = new LoginRequestDto(email, "pwd");

        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(email, req.password(), List.of())
        );
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // act + assert
        assertThrows(UserNotFoundException.class, () -> authService.login(req));

        verify(authenticationManager).authenticate(any());
        verify(userRepository).findByEmail(email);
        verify(jwtService, never()).generateToken(any());
    }
}
