FROM python:3.12-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential libjpeg62-turbo-dev zlib1g-dev \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

ENV PYTHONUNBUFFERED=1

# Dependências
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copia o código fonte organizado
# Copia a pasta 'app' inteira para dentro de '/app/app'
COPY app/ ./app/
# Copia o entrypoint para a raiz '/app/main.py'
COPY main.py .

# Define PYTHONPATH para o Python achar o módulo 'app'
ENV PYTHONPATH=/app

EXPOSE 5000
CMD ["python", "main.py"]