from minio import Minio
from app.config import Config
import io

class MinioClient:
    def __init__(self):
        self.client = Minio(
            Config.MINIO_URL,
            access_key=Config.MINIO_ACCESS_KEY,
            secret_key=Config.MINIO_SECRET_KEY,
            secure=Config.MINIO_SECURE
        )
        self.bucket = Config.MINIO_BUCKET

    def download(self, object_name):
        try:
            response = self.client.get_object(self.bucket, object_name)
            return response.read()
        finally:
            if 'response' in locals():
                response.close()
                response.release_conn()

    def upload(self, object_name, data_bytes, content_type='image/png'):
        if isinstance(data_bytes, bytes):
            data_stream = io.BytesIO(data_bytes)
        else:
            data_stream = data_bytes # Já é BytesIO

        data_stream.seek(0)
        length = data_stream.getbuffer().nbytes

        self.client.put_object(
            self.bucket,
            object_name,
            data=data_stream,
            length=length,
            content_type=content_type
        )