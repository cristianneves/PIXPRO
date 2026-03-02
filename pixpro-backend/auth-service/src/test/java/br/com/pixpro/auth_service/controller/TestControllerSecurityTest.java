package br.com.pixpro.auth_service.controller;

import br.com.pixpro.auth_service.model.Role;
import br.com.pixpro.auth_service.model.User;
import br.com.pixpro.auth_service.repository.UserRepository;
import br.com.pixpro.auth_service.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetails;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TestControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private User user;
    private User admin;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user = new User();
        user.setName("Regular User");
        user.setEmail("user@example.com");
        user.setPassword("pwd");
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);

        admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin@example.com");
        admin.setPassword("pwd");
        admin.setRole(Role.ROLE_ADMIN);
        userRepository.save(admin);
    }

    @Test
    @DisplayName("/api/test/hello returns greeting when authenticated with USER role")
    void helloEndpoint_WithUserToken_ReturnsGreeting() throws Exception {
        UserDetails principal = user;
        String token = jwtService.generateToken(principal);

        mockMvc.perform(get("/api/test/hello")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Olá, user@example.com!")));
    }

    @Test
    @DisplayName("/api/test/hello-admin returns 403 for USER role token")
    void helloAdminEndpoint_WithUserToken_Forbidden() throws Exception {
        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/test/hello-admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("/api/test/hello-admin returns greeting for ADMIN role token")
    void helloAdminEndpoint_WithAdminToken_Succeeds() throws Exception {
        String token = jwtService.generateToken(admin);

        mockMvc.perform(get("/api/test/hello-admin")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Olá, ADMIN admin@example.com!")));
    }
}