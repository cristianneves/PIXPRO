from app.clients.minio_client import MinioClient
from app.clients.stability_client import StabilityClient
from app.clients.gemini_client import GeminiClient
from app.utils import image_utils

class ImageProcessor:
    def __init__(self):
        self.storage = MinioClient()
        self.stability = StabilityClient()
        self.gemini = GeminiClient()

    def process(self, task_data):
        # Extrai as variáveis do dicionário
        image_id = task_data.get('imageId')
        task_type = task_data.get('taskType', 'IMG2IMG')
        model_name = task_data.get('modelName')
        prompt = task_data.get('prompt')
        original_path = task_data.get('originalStoragePath')

        result_buffer = None
        # Define um nome padrão para Text2Img (será sobrescrito se for Img2Img)
        output_filename = f"gen_{model_name}_{image_id}.png"

        print(f"  [Processor] ID: {image_id} | Tipo: {task_type} | Modelo: {model_name}")

        # --- Rota 1: Geração (Text2Img) ---
        if task_type == "TEXT2IMG":
            if model_name == "STABILITY_GENERATE":
                result_buffer = self.stability.generate_text2img(prompt)

            elif model_name in ["GEMINI_IMAGEN", "GEMINI-IMAGEN"]:
                result_buffer = self.gemini.generate_from_text_imagen(prompt)

            else:
                raise Exception(f"Modelo de geração '{model_name}' desconhecido.")

        # --- Rota 2: Edição (Img2Img) ---
        else:
            if not original_path:
                raise Exception("Tarefa IMG2IMG sem caminho de imagem original.")

            print(f"  [Processor] Baixando: {original_path}")
            original_bytes = self.storage.download(original_path)

            # Atualiza o nome do arquivo de saída para manter a referência
            output_filename = f"processed_{model_name}_{original_path}"

            if model_name == "STABILITY_DIFFUSION":
                result_buffer = self.stability.generate_img2img(prompt, original_bytes)

            elif model_name == "GEMINI_EDIT":
                result_buffer = self.gemini.edit_image(prompt, original_bytes)

            else:
                print("  [Processor] Fallback para Grayscale")
                result_buffer = image_utils.apply_grayscale_filter(original_bytes)

        # --- Salvar Resultado ---
        if not result_buffer:
            raise Exception("Nenhuma imagem gerada.")

        print(f"  [Processor] Uploading: {output_filename}")
        self.storage.upload(output_filename, result_buffer)

        return output_filename