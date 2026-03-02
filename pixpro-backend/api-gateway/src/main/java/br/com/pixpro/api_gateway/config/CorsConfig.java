package br.com.pixpro.api_gateway.config; // Certifique-se que este é o pacote certo

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered; // Importe este
import org.springframework.core.annotation.Order; // Importe este
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List; // Use java.util.List

@Configuration
public class CorsConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) // <-- ESTA É A MUDANÇA MÁGICA
    public CorsWebFilter corsWebFilter() {

        CorsConfiguration configuration = new CorsConfiguration();

        // Lista de origens permitidas
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // Métodos permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Cabeçalhos permitidos
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        // Permitir credenciais (cookies, etc.)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica a todas as rotas

        return new CorsWebFilter(source);
    }
}