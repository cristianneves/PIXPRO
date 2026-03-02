# Serviço de Autenticação (auth-service)

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.6-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Security](https://img.shields.io/badge/Spring_Security-JWT-red)

## 1. Visão Geral

O **Auth Service** é o responsável pela identidade e controle de acesso na plataforma PixPro. Ele gerencia o ciclo de vida dos usuários (registro, login, atualização, exclusão) e fornece tokens JWT (JSON Web Token) enriquecidos para autorizar o acesso aos demais microsserviços.

A aplicação segue uma arquitetura *stateless*, utiliza PostgreSQL para persistência e integra-se ao Eureka para Service Discovery.

## 2. Tecnologias

* **Java 17** & **Spring Boot 3**
* **Spring Security**: Autenticação e Autorização.
* **JJWT**: Geração e validação de Tokens.
* **Spring Data JPA**: Persistência de dados.
* **PostgreSQL**: Banco de dados relacional.
* **SpringDoc OpenAPI**: Documentação automática (Swagger).

## 3. Configuração (.env)

Para executar o serviço, é necessário configurar as variáveis de ambiente. Crie um arquivo `.env` na raiz do diretório `auth-service`:

```properties
# Configurações do Servidor
SERVER_PORT=8081

# Banco de Dados (PostgreSQL)
# Se rodar via Docker Compose, o host é 'postgres-auth'
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/pixpro_auth_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=admin

# Segurança (JWT)
# Deve ser a mesma chave usada no API Gateway e outros serviços
JWT_SECRET_KEY=a2a6293f7f22f87a87c061b52a201c8286add5f7a083f2a3f7f87c061b52a201

# Usuário Administrador Inicial (Criado automaticamente se não existir)
ADMIN_EMAIL=admin@pixpro.com
ADMIN_PASSWORD=admin1234

# Service Discovery
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://localhost:8761/eureka/
    ```

### Execução
Navegue até a pasta raiz do `auth-service` e execute:
```bash
mvn spring-boot:run 
```

O serviço estará disponível em http://localhost:8081.

## 4. Estrutura do Token JWT
   O token gerado contém claims personalizadas para facilitar o uso no Frontend e validação nos microsserviços:

### JSON

{
"sub": "user@email.com",    // E-mail do usuário
"userId": 123,              // ID numérico no banco de dados
"name": "Nome do Usuário",  // Nome de exibição
"roles": ["ROLE_USER"],     // Lista de permissões
"iat": 1715190000,          // Data de emissão
"exp": 1715276400           // Data de expiração (24h)
}

### API
A documentação completa e interativa da API está disponível via Swagger UI. Após iniciar o serviço, acesse:

http://localhost:8081/swagger-ui.html

## Como Executar

Via Docker Compose, na raiz do projeto pixpro-backend:

docker-compose up -d --build auth-service