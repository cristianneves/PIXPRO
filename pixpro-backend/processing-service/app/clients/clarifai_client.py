from clarifai_grpc.channel.clarifai_channel import ClarifaiChannel
from clarifai_grpc.grpc.api import resources_pb2, service_pb2, service_pb2_grpc
from clarifai_grpc.grpc.api.status import status_code_pb2
from app.config import Config

class ClarifaiClient:
    def __init__(self):
        self.configured = bool(Config.CLARIFAI_PAT)
        if self.configured:
            self.channel = ClarifaiChannel.get_grpc_channel()
            self.stub = service_pb2_grpc.V2Stub(self.channel)
            self.metadata = (('authorization', f'Key {Config.CLARIFAI_PAT}'),)

    def detect(self, image_bytes):
        if not self.configured:
            raise Exception("Clarifai PAT não configurado.")

        request_data = service_pb2.PostModelOutputsRequest()
        request_data.user_app_id.user_id = Config.CLARIFAI_USER_ID
        request_data.user_app_id.app_id = Config.CLARIFAI_APP_ID
        request_data.model_id = Config.CLARIFAI_MODEL_ID
        request_data.inputs.extend([
            resources_pb2.Input(
                data=resources_pb2.Data(
                    image=resources_pb2.Image(base64=image_bytes)
                )
            )
        ])

        response = self.stub.PostModelOutputs(request_data, metadata=self.metadata)

        if response.status.code != status_code_pb2.SUCCESS:
            raise Exception(f"Erro Clarifai: {response.status.description}")

        if len(response.outputs[0].data.regions) > 0:
                    return [region.data.concepts[0].name for region in response.outputs[0].data.regions]

        return [concept.name for concept in response.outputs[0].data.concepts]