package br.com.pixpro.auth_service.config;

import br.com.pixpro.auth_service.exception.UserNotFoundException;
import br.com.pixpro.auth_service.model.Role;
import br.com.pixpro.auth_service.model.User;
import br.com.pixpro.auth_service.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApplicationConfigTest {

    @Test
    @DisplayName("userDetailsService loads existing user")
    void userDetailsServiceLoadsUser() {
        UserRepository repo = mock(UserRepository.class);
        User u = new User();
        u.setId(1L);
        u.setName("X");
        u.setEmail("x@example.com");
        u.setPassword("p");
        u.setRole(Role.ROLE_USER);
        when(repo.findByEmail("x@example.com")).thenReturn(Optional.of(u));

        ApplicationConfig config = new ApplicationConfig(repo);
        UserDetailsService uds = config.userDetailsService();
        assertEquals("x@example.com", uds.loadUserByUsername("x@example.com").getUsername());
    }

    @Test
    @DisplayName("userDetailsService throws when user missing")
    void userDetailsServiceThrowsWhenMissing() {
        UserRepository repo = mock(UserRepository.class);
        when(repo.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        ApplicationConfig config = new ApplicationConfig(repo);
        UserDetailsService uds = config.userDetailsService();
        assertThrows(UserNotFoundException.class, () -> uds.loadUserByUsername("missing@example.com"));
    }

    @Test
    @DisplayName("authenticationProvider configured with password encoder")
    void authenticationProviderSetup() {
        UserRepository repo = mock(UserRepository.class);
        ApplicationConfig config = new ApplicationConfig(repo);
        var provider = config.authenticationProvider();
        assertNotNull(provider); // Provider exists
        PasswordEncoder encoder = config.passwordEncoder();
        assertTrue(encoder.matches("raw", encoder.encode("raw"))); // sanity check
    }
}
