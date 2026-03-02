## Executando o Projeto (Setup Rápido)

Esta seção explica como subir todo o backend (microserviços + infraestrutura) usando Docker, e como validar que está tudo funcionando.

### Pré‑requisitos

- Docker Desktop (ou Docker Engine) e Docker Compose
- Opcional (apenas se quiser rodar serviços localmente, sem Docker):
    - Java 17 (JDK)
    - Maven 3.8+
    - Node.js 18+ (se for rodar o frontend)

### Subir todo o backend

Você pode usar os scripts prontos do `package.json` ou rodar os comandos Docker diretamente.

Usando NPM (na raiz do repositório):

```bash
npm run docker:back-up         # Sobe toda a stack do backend (via pixpro-backend/docker-compose.yml)
npm run docker:back-build      # (Opcional) Rebuild das imagens
npm run docker:back-down       # Derruba tudo
```

Ou usando Docker Compose diretamente:

```bash
cd pixpro-backend
docker-compose up -d     # Sobe toda a stack
docker-compose ps        # Mostra status
docker-compose logs -f   # Acompanha logs de todos os serviços
```

### Serviços e URLs

- API Gateway: http://localhost:8080
- Eureka (Discovery): http://localhost:8761
- MinIO Console: http://localhost:9001 (user: `minioadmin` / pass: `minioadmin`)
- Auth Service: http://localhost:8081
- Project Service: http://localhost:8082
- Notification Service (WebSocket/HTTP): http://localhost:8083
- Processing Service (Flask/health): http://localhost:5000/health

Obs: Em tempo de execução, os serviços conversam entre si usando nomes de host do Compose (ex.: `kafka:29092`, `minio:9000`, `discovery-server:8761`).

### Ordem de inicialização (automática)

O `docker-compose.yml` já define dependências e healthchecks. Em resumo:

1. Infra (Zookeeper, Kafka, MinIO, Postgres)
2. discovery-server (Eureka)
3. auth-service, project-service, notification-service
4. api-gateway
5. processing-service

### Troubleshooting rápido

- Docker não está rodando: abra o Docker Desktop e aguarde ficar “Ready”.
- Ver logs de um serviço específico:
    ```bash
    cd pixpro-backend
    docker-compose logs -f api-gateway
    ```
- Limpar volumes (reset de dados):
    ```bash
    cd pixpro-backend
    docker-compose down -v
    ```
- `processing-service` com erro `NoBrokersAvailable`: aguarde ~1min. O serviço possui *retry* para conectar ao Kafka.

### Rodando um serviço localmente (sem Docker)

Exemplo com o `auth-service`:

```bash
cd pixpro-backend
./mvnw -f auth-service/pom.xml spring-boot:run
```

Certifique-se de ter a infraestrutura rodando via Docker (Kafka, MinIO, Postgres, Eureka) ou ajuste as variáveis de ambiente/`application.properties` para apontar para `localhost`.
