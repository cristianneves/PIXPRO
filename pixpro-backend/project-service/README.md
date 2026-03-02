# Serviço de Projetos (project-service)

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.6-brightgreen)

## 1. Visão Geral

Este microserviço é o núcleo para o gerenciamento de conteúdo do usuário na plataforma PixPro. Ele é responsável por todas as operações relacionadas a projetos e imagens.

Suas principais funcionalidades são:
- Gerenciar o ciclo de vida de projetos (Criar, Ler, Atualizar, Deletar).
- Lidar com o upload de imagens (únicas ou em lote) para um projeto.
- Gerenciar solicitações de geração de imagens via IA (Text-to-Image) sem upload prévio.
- Persistir os metadados das imagens em um banco de dados.
- Interagir com o serviço de armazenamento de objetos (MinIO) para salvar os arquivos.
- Orquestrar o fluxo de processamento assíncrono, publicando eventos no Kafka.
- Fornecer URLs seguras para download das imagens processadas.

## 2. Como Executar Localmente

### Pré-requisitos
- JDK 17
- Docker e Docker Compose
- Maven 3.8+

### Configuração
1.  Na pasta raiz do projeto (`pixpro-backend`), inicie as dependências com o comando:
    ```bash
    docker-compose up -d
    ```
2.  Crie um arquivo `.env` na raiz da pasta `project-service` e preencha com base no exemplo abaixo. **Este arquivo não deve ser enviado para o Git.**

    **Arquivo de exemplo `.env.example`:**
    ```properties
    # Porta da aplicação
    SERVER_PORT=8082

    # Conexão com o banco de dados (ver docker-compose.yml)
    SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/pixpro_project_db
    SPRING_DATASOURCE_USERNAME=postgres
    SPRING_DATASOURCE_PASSWORD=admin

    # Conexão com o Kafka (ver docker-compose.yml)
    SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092

    # Chave secreta para validação dos tokens JWT (deve ser a mesma do auth-service)
    JWT_SECRET_KEY=a2a6293f7f22f87a87c061b52a201c8286add5f7a083f2a3f7f87c061b52a201

    # Configuração do MinIO (ver docker-compose.yml)
    AWS_S3_ENDPOINT=http://localhost:9000
    AWS_S3_PUBLIC_ENDPOINT=http://localhost:9000
    AWS_S3_BUCKET_NAME=pixpro-images
    AWS_S3_ACCESS_KEY_ID=minioadmin
    AWS_S3_SECRET_ACCESS_KEY=minioadmin
    AWS_REGION=us-east-1

    # Endereço do Eureka Server
    EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
    ```

### Execução
Navegue até a pasta raiz do `project-service` e execute:
```bash
mvn spring-boot:run
```

O serviço estará disponível em http://localhost:8082.

### API
A documentação completa e interativa da API está disponível via Swagger UI. Após iniciar o serviço, acesse:

http://localhost:8082/swagger-ui.html

## 3. Testes e Cobertura

Para executar os testes com relatório de cobertura JaCoCo e validar os limites mínimos:

```bash
mvn verify
```

Após a execução, os relatórios ficam disponíveis em:

- HTML: `target/site/jacoco/index.html`
- XML: `target/site/jacoco/jacoco.xml`

Regras de cobertura (enforced no build):

- Instruções (INSTRUCTION) cobertas: mínimo 70%
- Branches (BRANCH) cobertos: mínimo 60%

Arquivos de configuração, DTOs e similares podem ser excluídos da checagem quando necessário.

### 4. Endpoints Principais

#### 📂 Projetos
| Método | Rota | Descrição |
| :--- | :--- | :--- |
| `POST` | `/api/projects` | Cria um novo projeto. |
| `GET` | `/api/projects` | Lista os projetos do usuário logado. |
| `GET` | `/api/projects/{id}` | Detalhes de um projeto específico. |
| `DELETE` | `/api/projects/{id}` | Remove um projeto e todas as suas imagens. |

#### 🖼️ Imagens & IA
| Método | Rota | Descrição | Payload Exemplo |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/projects/{id}/images` | **Upload (Img2Img):** Envia arquivo para edição/análise. | `MultipartFile` + `prompt` + `modelName` |
| `POST` | `/api/projects/{id}/generate` | **Geração (Text2Img):** Cria imagem do zero via prompt. | `{ "prompt": "...", "modelName": "..." }` |
| `GET` | `/api/projects/{pid}/images/{id}/download-url` | Gera URL temporária (presigned) para baixar o resultado. | - |

#### 🛡️ Administrativo
| Método | Rota | Descrição |
| :--- | :--- | :--- |
| `GET` | `/api/projects/admin/projects` | Lista global de todos os projetos (Requer ROLE_ADMIN). |
| `DELETE` | `/api/projects/admin/projects/{id}` | Exclusão forçada de projeto (Requer ROLE_ADMIN). |
| `GET` | `/api/projects/admin/stats` | Estatísticas gerais do sistema. |