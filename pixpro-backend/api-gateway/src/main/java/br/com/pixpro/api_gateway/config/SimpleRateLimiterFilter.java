package br.com.pixpro.api_gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SimpleRateLimiterFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(SimpleRateLimiterFilter.class);

    // Regras: 20 requisições por IP a cada 10 segundos
    private static final int MAX_REQUESTS = 20;
    private static final long TIME_WINDOW_MS = 10000; // 10 segundos

    // O nosso "contador" in-memory.
    // A Chave (String) é o endereço IP.
    // O Valor (List<Long>) é uma lista de timestamps (marcas de tempo) das requisições.
    private final Map<String, List<Long>> requestCounts = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. Obter o endereço IP do cliente.
        String ip = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
        long currentTime = System.currentTimeMillis();

        // 2. Obter (ou criar) a lista de timestamps para este IP.
        // Usamos CopyOnWriteArrayList para segurança em ambientes com múltiplas threads.
        List<Long> timestamps = requestCounts.computeIfAbsent(ip, k -> new CopyOnWriteArrayList<>());

        // 3. Remover timestamps que são mais antigos que a nossa janela de tempo (10 segundos).
        timestamps.removeIf(time -> (currentTime - time) > TIME_WINDOW_MS);

        // 4. Verificar o limite.
        if (timestamps.size() >= MAX_REQUESTS) {
            // Se o limite foi excedido, rejeita a requisição.
            logger.warn("!!! RATE LIMIT EXCEDIDO para o IP: {}. {} reqs em {}ms", ip, timestamps.size(), TIME_WINDOW_MS);

            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS); // Retorna 429
            return exchange.getResponse().setComplete(); // Encerra a requisição
        }

        // 5. Se estiver abaixo do limite, permite a requisição e regista o timestamp.
        timestamps.add(currentTime);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // Define a prioridade. Deve ser muito alta para rodar antes de outros filtros.
        // Nosso LoggingGlobalFilter é -1, então -2 roda antes dele.
        return -2;
    }
}