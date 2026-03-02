from PIL import Image, ImageOps
import io

def prepare_image_for_sdxl(image_bytes):
    """Redimensiona e corta a imagem para resoluções compatíveis com SDXL."""
    ALLOWED_DIMENSIONS = [
        (1024, 1024), (1152, 896), (1216, 832), (1344, 768), (1536, 640),
        (640, 1536), (768, 1344), (832, 1216), (896, 1152)
    ]
    try:
        img = Image.open(io.BytesIO(image_bytes))
        if img.mode in ("RGBA", "P"):
            img = img.convert("RGB")

        current_ratio = img.width / img.height
        closest_dim = min(ALLOWED_DIMENSIONS, key=lambda dim: abs((dim[0]/dim[1]) - current_ratio))

        print(f"  [Utils] Resize: {img.size} -> {closest_dim}")
        new_img = ImageOps.fit(img, closest_dim, method=Image.Resampling.LANCZOS)

        buffer = io.BytesIO()
        new_img.save(buffer, format="JPEG", quality=95)
        return buffer.getvalue()
    except Exception as e:
        print(f"  [Utils] Erro no resize: {e}")
        return image_bytes

def apply_grayscale_filter(image_bytes):
    """Filtro simples de fallback."""
    img = Image.open(io.BytesIO(image_bytes))
    grayscale = img.convert('L')
    buffer = io.BytesIO()
    grayscale.save(buffer, format='JPEG')
    buffer.seek(0)
    return buffer