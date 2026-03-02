from app.interfaces.web_server import start_server
from app.interfaces.kafka_consumer import start_worker

if __name__ == "__main__":
    print(">>> Iniciando PixPro Processing Service (Refatorado)...")

    # 1. Inicia Worker Kafka (Background)
    start_worker()

    # 2. Inicia Web Server (Bloqueante)
    start_server()