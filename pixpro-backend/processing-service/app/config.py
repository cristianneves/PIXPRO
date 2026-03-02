import os

class Config:
    # Kafka
    KAFKA_BROKER_URL = os.getenv('KAFKA_BROKER_URL', 'kafka:29092')
    TOPIC_PROCESSING = os.getenv('IMAGE_PROCESSING_TOPIC', 'image-processing-queue')
    TOPIC_RESULTS = os.getenv('IMAGE_RESULTS_TOPIC', 'image-processing-results')

    # MinIO
    MINIO_URL = os.getenv('MINIO_URL', 'minio:9000')
    MINIO_ACCESS_KEY = os.getenv('MINIO_ACCESS_KEY', 'minioadmin')
    MINIO_SECRET_KEY = os.getenv('MINIO_SECRET_KEY', 'minioadmin')
    MINIO_BUCKET = os.getenv('MINIO_BUCKET_NAME', 'pixpro-images')
    MINIO_SECURE = os.getenv('MINIO_SECURE', 'false').lower() == 'true'

    # IAs
    STABILITY_API_KEY = os.getenv('STABILITY_API_KEY')

    # IAs - Clarifai (Corrigindo o erro de atributo faltante)
    CLARIFAI_PAT = os.getenv('CLARIFAI_PAT')
    CLARIFAI_USER_ID = 'clarifai'
    CLARIFAI_APP_ID = 'main'
    CLARIFAI_MODEL_ID = 'general-image-detection'

    # Gemini (Google)
    GEMINI_API_KEY = os.getenv('GEMINI_API_KEY')

    # App
    PORT = int(os.getenv('PORT', '5000'))