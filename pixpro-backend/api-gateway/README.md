# API Gateway (api-gateway)

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.6-brightgreen)

## 1. Visão Geral

O **API Gateway** é a porta de entrada unificada para toda a plataforma PixPro. Construído com **Spring Cloud Gateway**, ele atua como um proxy reverso inteligente, direcionando todas as requisições externas (do frontend ou outros clientes) para o microserviço interno apropriado.

Suas principais responsabilidades são:
- **Roteamento (Routing):** Encaminhar requisições com base no caminho (`path`) para o serviço correto.
- **Descoberta de Serviço (Service Discovery):** Integrar-se com o `discovery-server` (Eureka) para encontrar dinamicamente a localização dos outros microserviços na rede.
- **Ponto Único de Acesso (Single Point of Entry):** Simplificar a arquitetura do lado do cliente e fornecer um local centralizado para implementar funcionalidades transversais, como logging, segurança, CORS e limitação de taxa (rate limiting).

## 2. Como Executar Localmente

### Pré-requisitos
- JDK 17
- Docker e Docker Compose
- Maven 3.8+

### Configuração
1.  Na pasta raiz do projeto (`pixpro-backend`), inicie todas as dependências (incluindo o `discovery-server` e os outros serviços) com o comando:
    ```bash
    docker-compose up -d
    ```
2.  As principais configurações, como as regras de roteamento, estão no arquivo `application.yml`. Para um ambiente local, as variáveis de ambiente necessárias são mínimas.

    **Arquivo de exemplo `.env.example`:**
    ```properties
    # Porta da aplicação. Esta é a porta principal para acessar o backend.
    SERVER_PORT=8080

    # Endereço do Eureka Server
    EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
    ```

### Execução
Navegue até a pasta raiz do `api-gateway` e execute:
```bash
mvn spring-boot:run
```

O Gateway estará disponível em http://localhost:8080 e pronto para receber requisições.

### Regras de Roteamento
As regras de roteamento são definidas no arquivo application.yml e utilizam o discovery-server para encontrar os serviços. O prefixo lb:// (Load Balancer) indica que a rota é resolvida via Eureka.

Abaixo estão as regras de roteamento atuais:

Caminho da Requisição 

(Path)	Serviço de Destino (Service ID)	Descrição

/api/auth/**	----- lb://auth-service	(Roteia para o Serviço de Autenticação.)

/api/projects/**	----- lb://project-service	(Roteia para o Serviço de Projetos.)

Exemplo: Uma requisição para http://localhost:8080/api/auth/login será automaticamente encaminhada para o auth-service em sua porta correspondente (ex: http://localhost:8081/api/auth/login).