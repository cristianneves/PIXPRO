import json
import threading
from kafka import KafkaConsumer, KafkaProducer
from flask import Flask, jsonify
from minio import Minio
from PIL import Image
import io

# --- Configurações ---
KAFKA_BROKER_URL = 'localhost:9092'
IMAGE_PROCESSING_TOPIC = 'image-processing-queue'
IMAGE_RESULTS_TOPIC = 'image-processing-results' # Novo tópico


MINIO_URL = 'localhost:9000'
MINIO_ACCESS_KEY = 'minioadmin'
MINIO_SECRET_KEY = 'minioadmin'
MINIO_BUCKET_NAME = 'pixpro-images'

# Cria o cliente do MinIO
minio_client = Minio(
    MINIO_URL,
    access_key=MINIO_ACCESS_KEY,
    secret_key=MINIO_SECRET_KEY,
    secure=False # False porque estamos rodando localmente sem HTTPS
)

# Cria a instância do Produtor Kafka
kafka_producer = KafkaProducer(
    bootstrap_servers=KAFKA_BROKER_URL,
    # Serializa o valor da mensagem de um dicionário para JSON (bytes)
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

# Cria a instância da aplicação Flask
app = Flask(__name__)

def process_image_task(image_data):
    image_id = image_data.get('imageId')
    original_path = image_data.get('originalStoragePath')
    processed_path = f"processed_{original_path}"
    status = "FAILED" # Define um status padrão de falha

    try:
        print(f"  [Processing imageId: {image_id}] Baixando do MinIO: {original_path}")
        # ... (lógica de download, processamento e upload) ...
        print(f"  [Processing imageId: {image_id}] Tarefa concluída com sucesso.")
        status = "COMPLETED"

    except Exception as e:
        print(f"\n!!! ERRO ao processar imageId {image_id}: {e}")
        # O status permanecerá "FAILED"

    finally:
        # Garante que a conexão de resposta seja fechada
        # ...

        # 5. Publica a mensagem de resultado no Kafka
        result_message = {
            "imageId": image_id,
            "status": status,
            "processedStoragePath": processed_path if status == "COMPLETED" else None
        }
        print(f"  [Processing imageId: {image_id}] Publicando resultado no tópico: {IMAGE_RESULTS_TOPIC}")
        kafka_producer.send(IMAGE_RESULTS_TOPIC, value=result_message)
        kafka_producer.flush() # Força o envio imediato da mensagem

def kafka_consumer_thread():
    """
    Função que cria e executa o consumidor Kafka em um loop.
    """
    print(">>> Iniciando thread do consumidor Kafka...")
    print(f">>> Escutando o tópico: {IMAGE_PROCESSING_TOPIC}")

    try:
        topics_to_subscribe = (IMAGE_PROCESSING_TOPIC,)

        consumer = KafkaConsumer(
            *topics_to_subscribe,
            bootstrap_servers=KAFKA_BROKER_URL,
            auto_offset_reset='earliest',
            group_id='image-processors-group-1',
            value_deserializer=lambda x: json.loads(x.decode('utf-8'))
        )

        for message in consumer:
            print("\n--- Nova mensagem recebida pela thread do Kafka! ---")
            process_image_task(message.value)

    except Exception as e:
        print(f"\n!!! Ocorreu um erro na thread do consumidor Kafka: {e}")

@app.route("/health", methods=['GET'])
def health_check():
    return jsonify({"status": "UP"}), 200

if __name__ == "__main__":
    kafka_thread = threading.Thread(target=kafka_consumer_thread, daemon=True)
    kafka_thread.start()

    print(">>> Iniciando servidor Flask...")
    app.run(host='0.0.0.0', port=5000, debug=False)