import requests
import base64
import io
from app.config import Config
from app.utils import image_utils

class StabilityClient:
    def __init__(self):
        if not Config.STABILITY_API_KEY:
            raise Exception("STABILITY_API_KEY ausente.")
        self.api_host = 'https://api.stability.ai'
        self.headers = {
            "Authorization": f"Bearer {Config.STABILITY_API_KEY}",
            "Accept": "application/json"
        }

    def generate_text2img(self, prompt):
        url = f"{self.api_host}/v1/generation/stable-diffusion-xl-1024-v1-0/text-to-image"
        payload = {
            "text_prompts": [{"text": prompt, "weight": 1}],
            "cfg_scale": 7, "height": 1024, "width": 1024, "samples": 1, "steps": 30
        }
        return self._post(url, json=payload)

    def generate_img2img(self, prompt, image_bytes):
        # Tratamento prévio da imagem
        clean_bytes = image_utils.prepare_image_for_sdxl(image_bytes)

        url = f"{self.api_host}/v1/generation/stable-diffusion-xl-1024-v1-0/image-to-image"
        data = {
            "image_strength": 0.35, "init_image_mode": "IMAGE_STRENGTH",
            "text_prompts[0][text]": prompt, "text_prompts[0][weight]": 1,
            "cfg_scale": 7, "samples": 1, "steps": 30
        }
        files = {"init_image": clean_bytes}
        return self._post(url, data=data, files=files)

    def _post(self, url, json=None, data=None, files=None):
        response = requests.post(url, headers=self.headers, json=json, data=data, files=files)
        if response.status_code != 200:
            raise Exception(f"Stability API Error ({response.status_code}): {response.text}")

        image_base64 = response.json()["artifacts"][0]["base64"]
        return io.BytesIO(base64.b64decode(image_base64))