# PixPro Backend

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.6-brightgreen)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2023.0.2-brightgreen)
![Docker](https://img.shields.io/badge/Docker-compose-blue)
![Kafka](https://img.shields.io/badge/Apache_Kafka-3.2-black)

## 1. Sobre o Projeto

Este repositório contém todo o backend da **PixPro**, uma plataforma inteligente de processamento de imagens. A arquitetura é baseada em **microsserviços**, projetada para ser rápida, escalável e resiliente, permitindo que os usuários apliquem melhorias, detectem objetos e transformem imagens com modelos avançados de inteligência artificial.

## 2. Arquitetura

O sistema é composto por um conjunto de microsserviços independentes que se comunicam de forma síncrona (via REST) e assíncrona (via mensageria com Kafka).

### Componentes

Cada pasta neste repositório representa um microsserviço com uma responsabilidade única:

- **`api-gateway`**: A porta de entrada unificada para todas as requisições. Roteia o tráfego para os serviços apropriados.
- **`discovery-server`**: Registro de serviços (Eureka) que permite que os microsserviços se encontrem dinamicamente.
- **`auth-service`**: Gerencia a identidade do usuário: registro, login e geração de tokens JWT.
- **`project-service`**: Lida com a lógica de negócio principal: gerenciamento de projetos, upload de imagens e orquestração do fluxo de processamento.
- **`notification-service`**: Envia notificações em tempo real para os clientes via WebSockets sobre o status do processamento de imagens.

## 3. Tecnologias

- **Backend:** Java 17, Spring Boot, Spring Cloud (Gateway, Eureka), Spring Security, Spring Data JPA
- **Banco de Dados:** PostgreSQL
- **Mensageria:** Apache Kafka
- **Armazenamento de Objetos:** MinIO (Compatível com S3)
- **Infraestrutura e Build:** Docker, Docker Compose, Maven

## 4. Como Executar o Ambiente Completo

Siga os passos abaixo para iniciar toda a pilha de backend localmente.

### Pré-requisitos
- JDK 17
- Docker e Docker Compose
- Maven 3.8+

### Passo 1: Subir tudo via Docker Compose
A infraestrutura base (Postgres, Kafka, MinIO, Zookeeper) e os microsserviços são gerenciados pelo Docker Compose.

```bash
docker-compose up -d           # sobe toda a stack
docker-compose ps              # status
docker-compose logs -f         # logs
```

### Passo 2: (Opcional) Rebuild das imagens
```bash
docker-compose build
```

### Passo 3: URLs úteis
- API Gateway: http://localhost:8080
- Eureka (Discovery): http://localhost:8761
- MinIO Console: http://localhost:9001 (minioadmin/minioadmin)
- Auth Service: http://localhost:8081
- Project Service: http://localhost:8082
- Notification Service: http://localhost:8083
- Processing Service (health): http://localhost:5000/health

## Acesso à Aplicação
API Gateway (Ponto de Entrada Principal): http://localhost:8080

Dashboard do Eureka: http://localhost:8761

Console do MinIO: http://localhost:9001

## 5. Dicas de Desenvolvimento

### Rodar um serviço individual (sem Docker)
```bash
./mvnw -f auth-service/pom.xml spring-boot:run
```

Garanta que a infraestrutura (Kafka, MinIO, Postgres, Eureka) esteja ativa via Docker, ou adapte `application.properties` para `localhost`.

### Troubleshooting
- `NoBrokersAvailable` no processing-service: o serviço possui *retry* automático; aguarde ~1min após subir Kafka.
- Reset geral (apaga dados dos volumes):
	```bash
	docker-compose down -v
	```