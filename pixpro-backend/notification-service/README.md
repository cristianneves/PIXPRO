# Serviço de Notificações (notification-service)

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.6-brightgreen)

## 1. Visão Geral

Este microserviço é responsável por fornecer feedback em tempo real aos usuários da plataforma PixPro. Ele atua como uma ponte entre os eventos assíncronos do backend e a interface do usuário.

Sua principal funcionalidade é:
- Consumir eventos de tópicos do Kafka (ex: resultados do processamento de imagens).
- Enviar notificações para os usuários corretos através de uma conexão WebSocket segura e persistente.

Este serviço não possui um banco de dados, pois seu estado é volátil e baseado nas conexões ativas dos usuários.

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
2.  Crie um arquivo `.env` na raiz da pasta `notification-service` e preencha com base no exemplo abaixo. **Este arquivo não deve ser enviado para o Git.**

    **Arquivo de exemplo `.env.example`:**
    ```properties
    # Porta da aplicação
    SERVER_PORT=8083

    # Conexão com o Kafka (ver docker-compose.yml)
    SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
    SPRING_KAFKA_CONSUMER_GROUP_ID=notification-service-group

    # Chave secreta para validação dos tokens JWT (deve ser a mesma do auth-service)
    JWT_SECRET_KEY=a2a6293f7f22f87a87c061b52a201c8286add5f7a083f2a3f7f87c061b52a201

    # Endereço do Eureka Server
    EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
    ```

### Execução
Navegue até a pasta raiz do `notification-service` e execute:
```bash
mvn spring-boot:run
```

O serviço estará disponível em http://localhost:8083.

### API WebSocket
Este serviço expõe um único endpoint WebSocket para a comunicação em tempo real.

##### Endpoint de Conexão

ws://localhost:8083/ws/notifications

##### Autenticação

A conexão WebSocket deve ser autenticada. Para isso, o cliente (frontend) precisa enviar o token JWT válido (obtido do auth-service) como um parâmetro de query na URL de conexão.

##### Formato da URL de Conexão:

ws://localhost:8083/ws/notifications?token=<SEU_TOKEN_JWT_AQUI>
Se o token for ausente ou inválido, a conexão será rejeitada.

##### Mensagens (Servidor → Cliente)

Após a conexão, o serviço enviará mensagens para o cliente sempre que um evento relevante ocorrer. A comunicação é unidirecional (apenas o servidor envia mensagens).

Exemplo de Payload da Mensagem de Notificação:
Quando o status de uma imagem é atualizado, o cliente receberá uma mensagem JSON com o seguinte formato:

##### JSON

{
  "type": "PROCESSING_UPDATE",
  "imageId": 123,
  "status": "COMPLETED"
}

O cliente deve usar o campo type para determinar como processar a notificação.

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

Arquivos de configuração e DTOs podem ser excluídos da checagem quando necessário.