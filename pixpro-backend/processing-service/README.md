# Serviço de Processamento (processing-service)

![Python](https://img.shields.io/badge/Python-3.12-blue)
![Flask](https://img.shields.io/badge/Flask-API-green)
![Kafka](https://img.shields.io/badge/Apache_Kafka-Worker-black)
![MinIO](https://img.shields.io/badge/MinIO-Storage-red)

## 1. Visão Geral

O **Processing Service** é o motor de inteligência artificial da plataforma PixPro. Ele foi refatorado para uma **arquitetura híbrida e modular**, capaz de lidar tanto com requisições em tempo real quanto com processamento pesado em segundo plano.

Suas responsabilidades incluem:

1. **Deteção de Objetos (Síncrona):** API REST para análise imediata de imagens via Google Gemini ou Clarifai.
2. **Geração e Edição de Imagens (Assíncrona):** Worker Kafka que processa tarefas de *Text-to-Image* e *Image-to-Image* usando Stability AI e Google Gemini.

## 2. Tecnologias e Bibliotecas

- **Linguagem:** Python 3.12
- **Framework Web:** Flask (para endpoints síncronos e Health Check)
- **Mensageria:** `kafka-python-ng` (Consumo e produção de eventos)
- **Armazenamento:** `minio` (Download e Upload de ativos)
- **Processamento de Imagem:** `Pillow` (PIL)
- **Integrações de IA:**
    - **Google Gemini:** `google-generativeai` (Visão e Geração)
    - **Stability AI:** API REST via `requests` (SDXL e SD 1.5)
    - **Clarifai:** `clarifai-grpc` (Deteção Legacy/Backup)

## 3. Arquitetura do Projeto

O projeto segue uma estrutura modular para facilitar a manutenção e a adição de novas IAs:

processing-service/
├── app/
│   ├── clients/           # Adaptadores (Gemini, Stability, MinIO)
│   ├── core/              # Utilitários (Resize, Filtros)
│   ├── interfaces/        # Entradas (API Flask e Worker Kafka)
│   ├── processors/        # Lógica de decisão (Maestro)
│   └── config.py          # Configurações
├── main.py                # Entrypoint
├── Dockerfile
└── requirements.txt


## 4. Configuração e Execução

### Variáveis de Ambiente (.env)

Para que o serviço funcione, as seguintes variáveis devem ser configuradas no `docker-compose.yml` ou num arquivo `.env`:

#### Infraestrutura
KAFKA_BROKER_URL=kafka:29092
MINIO_URL=minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

#### Chaves de API (IAs)
GEMINI_API_KEY=AIzaSy...      # Necessário para Deteção e Geração (Imagen 3)
STABILITY_API_KEY=sk-...      # Necessário para SDXL e SD 1.5
CLARIFAI_PAT=...              # Necessário para Deteção (Backup)


### Executando via Docker (Recomendado)

Na raiz do projeto **pixpro-backend**:

```bash
docker-compose up -d --build processing-service
```

### Executando Localmente (Para Desenvolvimento)

Crie e ative o ambiente virtual:

 python -m venv venv
 Windows: .\venv\Scripts\activate
 Linux/Mac: source venv/bin/activate

##### Instale as dependências:

pip install -r requirements.txt

##### Configure as variáveis de ambiente no seu terminal (ou IDE).

##### Execute o ponto de entrada:

#### É necessário definir o PYTHONPATH para encontrar o módulo 'app'

set PYTHONPATH=.
python main.py


### 5. Funcionalidades e Endpoints

#### A. API Síncrona (HTTP)
Exposta na porta 5000 (mapeada via API Gateway).

Método	Rota	Descrição
GET	    /health	Verifica se o serviço está online.
POST	/detect	Envia uma imagem para análise e retorna objetos detectados. 

Parâmetro opcional: ?provider=clarifai (padrão: gemini).

#### B. Worker Assíncrono (Kafka)

O serviço escuta o tópico image-processing-queue e suporta os seguintes tipos de tarefa:

TEXT2IMG (Geração)
Gera uma imagem do zero a partir de um prompt.
Modelos: STABILITY_GENERATE, GEMINI_GENERATE.

IMG2IMG (Edição)
Baixa uma imagem original do MinIO e a modifica com base no prompt.
Modelos: STABILITY_DIFFUSION (SDXL), STABILITY_FAST (SD 1.5), GEMINI_EDIT.

### 6. Fluxo de Dados (Worker)

Leitura: o kafka_consumer recebe a mensagem JSON.

Decisão: o ImageProcessor identifica taskType e modelName.

Execução:

-Se for edição, baixa a imagem do MinIO.

-Chama o cliente de IA apropriado (Stability ou Gemini).

-Recebe os bytes da imagem processada.

-Persistência: salva o resultado no MinIO.

-Notificação: publica o resultado (status=COMPLETED + caminho do arquivo) no tópico image-processing-results.