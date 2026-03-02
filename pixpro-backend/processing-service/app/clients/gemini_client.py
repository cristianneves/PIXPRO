from google import genai
from google.genai import types
from PIL import Image
import io
from app.config import Config

class GeminiClient:
    def __init__(self):
        self.client = None # Inicializa como None para evitar erro de atributo
        self.configured = False

        if Config.GEMINI_API_KEY:
            try:
                # Inicializa o cliente único da nova SDK
                self.client = genai.Client(api_key=Config.GEMINI_API_KEY)
                self.configured = True
                print("  [Gemini] Cliente configurado com sucesso.")
            except Exception as e:
                print(f"  [Gemini] Erro na inicialização: {e}")
        else:
            print("  [Gemini] AVISO: API Key não encontrada.")

    def detect(self, image_bytes):
        """Fluxo 1: Deteção (Migrado para SDK Nova)"""
        if not self.configured or not self.client:
            raise Exception("Gemini API Key ausente ou cliente não inicializado.")

        try:
            image = Image.open(io.BytesIO(image_bytes))
            prompt = "Liste os 5 principais objetos nesta imagem. Responda apenas com as palavras separadas por vírgula, em português."

            # Nova sintaxe para gerar conteúdo (texto/visão)
            response = self.client.models.generate_content(
                model='gemini-2.5-flash', # Modelo mais rápido e estável para visão
                contents=[prompt, image]
            )

            return [tag.strip() for tag in response.text.split(',')]

        except Exception as e:
            print(f"  [Gemini Vision] Erro: {e}")
            raise Exception(f"Erro Gemini Vision: {e}")

    def generate_from_text_imagen(self, prompt):
        """Fluxo 2: Geração de Imagem (Via Gemini 2.0 Flash Multimodal)"""
        if not self.configured or not self.client:
            raise Exception("Gemini API Key ausente ou cliente não inicializado.")

        # Modelo que você confirmou ter acesso
        model_name = 'gemini-2.5-flash-image'
        print(f"  [IA] Executando: {model_name} (Text-to-Image)")

        try:
            # O segredo: Usamos generate_content, mas pedimos imagem na config
            response = self.client.models.generate_content(
                model=model_name,
                contents=prompt,
                config=types.GenerateContentConfig(
                    response_modalities=["IMAGE"], # Solicita imagem explicitamente
                    safety_settings=[types.SafetySetting(
                        category="HARM_CATEGORY_HATE_SPEECH",
                        threshold="BLOCK_ONLY_HIGH"
                    )]
                )
            )

            # A resposta vem em parts.inline_data na nova SDK
            if not response.candidates or not response.candidates[0].content.parts:
                raise Exception("Gemini não retornou conteúdo.")

            for part in response.candidates[0].content.parts:
                if part.inline_data:
                    # Sucesso! Temos a imagem em bytes.
                    image_bytes = part.inline_data.data
                    return io.BytesIO(image_bytes)

            # Se chegou aqui, não achou imagem (provavelmente retornou texto de recusa)
            text_response = response.text if response.text else "Sem resposta de texto."
            raise Exception(f"Gemini não gerou imagem. Resposta: {text_response}")

        except Exception as e:
            print(f"  [Gemini Gen] Erro: {e}")
            raise Exception(f"Erro na geração do Gemini: {e}")


    def edit_image(self, prompt, image_bytes):
        """
        Fluxo 3: Edição de Imagem (Img2Img) via Gemini 2.5 Flash Image.
        Envia a imagem original + prompt e recebe uma imagem nova.
        """
        if not self.configured or not self.client:
            raise Exception("Gemini API Key ausente ou cliente não inicializado.")

        # Modelo específico para edição (da sua lista)
        model_name = 'gemini-2.5-flash-image'
        print(f"  [IA] Executando: {model_name} (Img2Img)")

        try:
            # Carrega a imagem original
            input_image = Image.open(io.BytesIO(image_bytes))

            # O Gemini usa generate_content para edição
            # Enviamos [prompt, imagem] como conteúdo
            response = self.client.models.generate_content(
                model=model_name,
                contents=[prompt, input_image],
                config=types.GenerateContentConfig(
                    response_modalities=["IMAGE"], # Força a resposta ser uma imagem
                    safety_settings=[types.SafetySetting(
                        category="HARM_CATEGORY_HATE_SPEECH",
                        threshold="BLOCK_ONLY_HIGH"
                    )]
                )
            )

            # Extrai a imagem da resposta (igual à geração de texto)
            if not response.candidates or not response.candidates[0].content.parts:
                raise Exception("Gemini não retornou conteúdo.")

            for part in response.candidates[0].content.parts:
                if part.inline_data:
                    return io.BytesIO(part.inline_data.data)

            text_response = response.text if response.text else "Sem resposta."
            raise Exception(f"Gemini recusou editar a imagem. Resposta: {text_response}")

        except Exception as e:
            print(f"  [Gemini Edit] Erro: {e}")
            raise Exception(f"Erro na edição do Gemini: {e}")