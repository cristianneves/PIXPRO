import json
import threading
import time
from kafka import KafkaConsumer, KafkaProducer, KafkaAdminClient
from kafka.admin import NewTopic
from kafka.errors import NoBrokersAvailable
from app.config import Config
from app.processors.image_processor import ImageProcessor

class KafkaWorker:
    def __init__(self):
        self.processor = ImageProcessor()
        self.producer = None

    def _setup_producer(self):
        for _ in range(12):
            try:
                self.producer = KafkaProducer(
                    bootstrap_servers=Config.KAFKA_BROKER_URL,
                    value_serializer=lambda v: json.dumps(v).encode('utf-8')
                )
                print("[Kafka] Produtor conectado.")

                # Cria tópicos se não existirem
                try:
                    admin = KafkaAdminClient(bootstrap_servers=Config.KAFKA_BROKER_URL)
                    topics = [
                        NewTopic(name=Config.TOPIC_PROCESSING, num_partitions=1, replication_factor=1),
                        NewTopic(name=Config.TOPIC_RESULTS, num_partitions=1, replication_factor=1)
                    ]
                    admin.create_topics(new_topics=topics)
                except Exception:
                    pass # Tópicos já existem
                return
            except NoBrokersAvailable:
                print("[Kafka] Aguardando broker...")
                time.sleep(5)
        print("!!! ERRO: Falha ao conectar Kafka.")

    def start(self):
        self._setup_producer()
        consumer = KafkaConsumer(
            Config.TOPIC_PROCESSING,
            bootstrap_servers=Config.KAFKA_BROKER_URL,
            group_id='processing-group-v1',
            value_deserializer=lambda x: json.loads(x.decode('utf-8'))
        )
        print(f">>> Consumidor ouvindo: {Config.TOPIC_PROCESSING}")

        for msg in consumer:
            data = msg.value
            try:
                filename = self.processor.process(data)
                self._send_status(data, "COMPLETED", filename)
            except Exception as e:
                print(f"!!! Erro tarefa {data.get('imageId')}: {e}")
                self._send_status(data, "FAILED")

    def _send_status(self, task_data, status, path=None):
        if not self.producer: return
        msg = {
            "imageId": task_data.get('imageId'),
            "userId": task_data.get('userId'),
            "status": status,
            "processedStoragePath": path
        }
        self.producer.send(Config.TOPIC_RESULTS, value=msg)
        self.producer.flush()

def start_worker():
    worker = KafkaWorker()
    t = threading.Thread(target=worker.start, daemon=True)
    t.start()