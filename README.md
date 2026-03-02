# Guia de Contribuição - PixPro Backend

Este documento define os padrões e as melhores práticas para contribuir com o código do projeto PixPro. O objetivo é manter um histórico de versionamento limpo, legível e automatizável.

## 1. Padrão de Nomenclatura de Branches

Toda nova branch **deve** seguir um padrão de prefixo baseado em sua intenção. Nós adotamos uma versão simplificada do GitFlow.

**Regra (Regex):** `^(feature|bugfix|hotfix)/.+$`

Isso significa que o nome da sua branch deve começar com um dos seguintes prefixos, seguido por uma barra e uma descrição curta em *kebab-case* (letras minúsculas separadas por hífen).

### Prefixos Permitidos

* **`feature/`**: Para adicionar, refatorar ou remover uma funcionalidade.
    * `feature/adicionar-paginacao-projetos`
    * `feature/refatorar-jwt-service`

* **`bugfix/`**: Para corrigir um bug que não está em produção (normalmente em *development* ou *staging*).
    * `bugfix/corrigir-erro-upload-imagem`
    * `bugfix/calculo-incorreto-notificacao`

* **`hotfix/`**: Para correções urgentes de bugs encontrados em produção.
    * `hotfix/ajuste-variavel-ambiente-minio`

### Exemplos

| Nome da Branch | Válido? | Notas |
| :--- | :--- | :--- |
| `feature/novo-endpoint-admin` | **Sim** | |
| `bugfix/login-nao-funciona` | **Sim** | |
| `feature/auth-service/login` | **Sim** | (Embora o microserviço não seja obrigatório no nome) |
| `feature/` | **Não** | Falta a descrição. |
| `minha-branch-pessoal` | **Não** | Prefixo inválido. |

---

## 2. Padrão de Mensagens de Commit (Conventional Commits)

Nós **exigimos** o uso do padrão [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/). Isso é crucial para um histórico legível e para futuras automações (como geração de *changelogs*).

**Regra (Regex):** `^(feat|fix|chore|docs|refactor|test)\([a-z0-9-]+\): .+`

### Estrutura do Commit

A sua mensagem de commit **deve** seguir este formato:
`tipo(escopo): mensagem`

---

#### `tipo`: Qual o propósito da mudança?

* **`feat`**: (Feature) Uma nova funcionalidade (ex: `feat(auth-service): ...`).
* **`fix`**: (Bug Fix) Uma correção de bug (ex: `fix(project-service): ...`).
* **`docs`**: (Documentation) Mudanças apenas na documentação (`.md`, comentários de código).
* **`chore`**: (Chore) Manutenção, tarefas de build, CI/CD, `docker-compose.yml`, `.gitignore`.
* **`refactor`**: (Refactor) Refatoração de código que não corrige um bug nem adiciona uma feature.
* **`test`**: (Test) Adição ou correção de testes.

---

#### `escopo`: Onde a mudança ocorreu?

O escopo **deve** ser o nome do microserviço ou componente afetado, em letras minúsculas.

* `auth-service`
* `project-service`
* `api-gateway`
* `notification-service`
* `discovery-server`
* `processing-service`
* `root` (Para arquivos na raiz, como `docker-compose.yml` ou `CONTRIBUTING.md`).

---

#### `mensagem`: O que foi feito?

Uma descrição curta, no imperativo (ex: "adiciona" e não "adicionado"), começando com letra minúscula.

### Exemplos

| Mensagem de Commit | Válido? | Notas |
| :--- | :--- | :--- |
| `feat(auth-service): adiciona endpoint de registro de admin` | **Sim** | |
| `fix(project-service): corrige erro de permissão no upload` | **Sim** | |
| `docs(root): atualiza o README principal com novo setup` | **Sim** | |
| `chore(api-gateway): atualiza versão do spring-cloud` | **Sim** | |
| `refactor(project-service): move logica de storage para servico` | **Sim** | |
| `Corrigido o bug do login` | **Não** | Fora do padrão. |
| `feat: nova feature` | **Não** | Falta o escopo. |
| `feat(Projeto): adiciona feature` | **Não** | Escopo deve ser minúsculo (ex: `project-service`). |

---

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
