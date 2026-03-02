package br.com.pixpro.auth_service.config;

import br.com.pixpro.auth_service.model.Role;
import br.com.pixpro.auth_service.model.User;
import br.com.pixpro.auth_service.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private UserDetails buildUser(String email) {
        User u = new User();
        u.setId(1L);
        u.setName("Tester");
        u.setEmail(email);
        u.setPassword("pwd");
        u.setRole(Role.ROLE_USER);
        return u;
    }

    @Test
    @DisplayName("No Authorization header -> no authentication set")
    void noAuthorizationHeader() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    @DisplayName("Non-Bearer header -> skipped")
    void nonBearerHeader() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Basic abc123");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    @DisplayName("Bearer header but username extraction returns null -> no auth")
    void bearerHeaderUsernameNull() throws Exception {
        when(jwtService.extractUsername("token123")).thenReturn(null);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer token123");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService).extractUsername("token123");
        verifyNoInteractions(userDetailsService);
        verify(jwtService, times(0)).isTokenValid(any(), any());
    }

    @Test
    @DisplayName("Authentication already present -> skips validation")
    void authenticationAlreadyPresent() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("pre", null, java.util.List.of()));
        when(jwtService.extractUsername("tok")) .thenReturn("user@example.com");

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer tok");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        // Should keep existing authentication
        assertEquals("pre", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(jwtService).extractUsername("tok");
        verifyNoInteractions(userDetailsService);
        verify(jwtService, times(0)).isTokenValid(any(), any());
    }

    @Test
    @DisplayName("Valid token -> sets authentication in context")
    void validTokenSetsAuthentication() throws Exception {
        UserDetails user = buildUser("valid@example.com");
        when(jwtService.extractUsername("validToken")).thenReturn("valid@example.com");
        when(userDetailsService.loadUserByUsername("valid@example.com")).thenReturn(user);
        when(jwtService.isTokenValid("validToken", user)).thenReturn(true);

        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer validToken");
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("valid@example.com", ((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
        verify(jwtService).extractUsername("validToken");
        verify(userDetailsService).loadUserByUsername("valid@example.com");
        verify(jwtService).isTokenValid("validToken", user);
    }
}
