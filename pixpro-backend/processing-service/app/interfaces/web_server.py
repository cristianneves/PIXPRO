from flask import Flask, jsonify, request
from app.config import Config
from app.clients.gemini_client import GeminiClient
from app.clients.clarifai_client import ClarifaiClient

app = Flask(__name__)

# Instancia os clientes
gemini = GeminiClient()
clarifai = ClarifaiClient()

@app.route("/health", methods=['GET'])
def health_check():
    return jsonify({"status": "UP"}), 200

@app.route("/detect", methods=['POST'])
def detect_objects():
    if 'file' not in request.files:
        return jsonify({"error": "Nenhum arquivo enviado"}), 400

    # Lê o parâmetro 'provider' da URL (padrão é 'gemini')
    provider = request.args.get('provider', 'gemini').lower()

    file = request.files['file']
    file_bytes = file.read()

    try:
        detections = []

        if provider == 'clarifai':
            print(">>> Usando Provider: Clarifai")
            detections = clarifai.detect(file_bytes)
        else:
            print(">>> Usando Provider: Gemini")
            detections = gemini.detect(file_bytes)

        return jsonify({
            "provider": provider,
            "detected_objects": detections
        })

    except Exception as e:
        print(f"!!! ERRO /detect: {e}")
        return jsonify({"error": str(e)}), 500

def start_server():
    app.run(host='0.0.0.0', port=Config.PORT, debug=False)