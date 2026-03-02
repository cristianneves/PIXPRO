package br.com.pixpro.auth_service.controller;

import br.com.pixpro.auth_service.dto.LoginRequestDto;
import br.com.pixpro.auth_service.dto.RegisterRequestDto;
import br.com.pixpro.auth_service.service.AuthService;
import br.com.pixpro.auth_service.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false) // disable security filters for MVC slice tests
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // Mock security-related beans required by JwtAuthenticationFilter so slice can load context
    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("POST /api/auth/register returns 201 on success")
    void register_ReturnsCreated() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("Alice", "alice@example.com", "pwd");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string("Usuário registrado com sucesso!"));

        verify(authService).registerUser(eq("Alice"), eq("alice@example.com"), eq("pwd"));
    }

    @Test
    @DisplayName("POST /api/auth/login returns token on success")
    void login_ReturnsToken() throws Exception {
        LoginRequestDto dto = new LoginRequestDto("bob@example.com", "pwd");
        when(authService.login(any(LoginRequestDto.class))).thenReturn("jwt-token");

        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("jwt-token")));

        verify(authService).login(any(LoginRequestDto.class));
    }
}
