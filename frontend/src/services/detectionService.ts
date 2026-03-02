import { apiClient } from './apiClient';

interface DetectionResponse {
  provider: string;
  detected_objects: string[];
}

export const detectionService = {
  detectObjects: async (file: File, provider: string = 'gemini'): Promise<string[]> => {
    const formData = new FormData();
    
    // O backend Python espera request.files['file']
    formData.append('file', file); 

    // Faz a requisição POST
    const response = await apiClient.post<DetectionResponse>(
      `/api/detect?provider=${provider}`, 
      formData, 
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );

    // Retorna apenas o array de strings que o frontend espera-
    return response.data.detected_objects;
  },
};