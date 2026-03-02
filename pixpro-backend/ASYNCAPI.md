# PixPro AsyncAPI Documentation

## Resumo do Projeto

Este documento apresenta a implementação da especificação AsyncAPI para os hubs WebSocket do projeto PixPro, incluindo validação automatizada em CI/CD.

## Estrutura Implementada

### 📋 Arquivo AsyncAPI (`asyncapi.yaml`)
- **Especificação**: AsyncAPI 3.0.0
- **Hub Principal**: `/ws/notifications` (WebSocket para notificações em tempo real)
- **Canal Kafka**: `image-processing-results` (comunicação interna entre serviços)
- **Segurança**: JWT Bearer authentication
- **Esquemas**: Definição completa de mensagens para processamento de imagens

### 🔍 Validação Local
```bash
# Instalação do AsyncAPI CLI
npm install -g @asyncapi/cli

# Validação do schema
asyncapi validate asyncapi.yaml
```

### 📖 Documentação HTML
- Gerada automaticamente em `docs/index.html`
- Interface responsiva com índice navegável
- Exemplos de uso e esquemas detalhados
- Informações de segurança e endpoints

### 🔧 Pipeline CI/CD (GitLab)
```yaml
# Estágio de validação adicionado
stages:
  - validate  # <- Nova etapa
  - test
  - package
  - docker

# Job de validação AsyncAPI
asyncapi:validate:
  stage: validate
  image: node:18-alpine
  script:
    - asyncapi validate pixpro-backend/asyncapi.yaml
```

## Arquitetura WebSocket

### Hub de Notificações (`/ws/notifications`)
- **Protocolo**: WebSocket (ws/wss)
- **Autenticação**: JWT via cabeçalho Authorization durante handshake
- **Handler**: `NotificationWebSocketHandler.java`
- **Interceptor**: `JwtHandshakeInterceptor.java`
- **Porta**: 8082 (notification-service)

### Fluxo de Dados
1. **Processing Service** processa imagem
2. **Kafka** transporta resultado via tópico `image-processing-results`
3. **Notification Service** consome mensagem Kafka
4. **WebSocket** envia notificação em tempo real para o usuário específico

### Mensagens Definidas

#### ProcessingUpdate (WebSocket → Frontend)
```json
{
  "type": "PROCESSING_UPDATE",
  "imageId": 12345,
  "status": "COMPLETED|PROCESSING|FAILED",
  "processedAt": "2025-11-20T15:30:00Z"
}
```

#### ImageProcessingResult (Kafka interno)
```json
{
  "userId": 1001,
  "imageId": 12345,
  "status": "COMPLETED",
  "originalUrl": "https://storage.pixpro.com/original/12345.jpg",
  "processedUrl": "https://storage.pixpro.com/processed/12345.jpg",
  "metadata": {...}
}
```

## Benefícios Implementados

✅ **Documentação Padronizada**: Schema AsyncAPI serve como contrato único entre frontend/backend  
✅ **Validação Automática**: Pipeline quebra se WebSocket API for alterada incorretamente  
✅ **Documentação Visual**: Interface HTML auto-gerada facilita integração de novos desenvolvedores  
✅ **Versionamento**: Mudanças na API são rastreadas via controle de versão do schema  
✅ **Conformidade**: Padrão AsyncAPI garante compatibilidade com ferramentas do ecossistema

## Como Usar

1. **Desenvolvedores Frontend**: Consultar `docs/index.html` para entender mensagens WebSocket
2. **Mudanças na API**: Atualizar `asyncapi.yaml` antes de modificar código WebSocket
3. **CI/CD**: Pipeline automaticamente valida mudanças no schema
4. **Documentação**: Regenerada a cada commit que altera o AsyncAPI
