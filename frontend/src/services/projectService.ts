import { apiClient } from './apiClient';
import { Project, CreateProjectRequest, UpdateProjectRequest, ImageMetadata } from '~/types';

const BASE_URL = '/api/projects';

export const projectService = {
  /**
   * Lista todos os projetos do usuário autenticado.
   * @returns Uma promessa que resolve para uma lista de projetos.
   */
  listProjects: async (): Promise<Project[]> => {
    const response = await apiClient.get<Project[]>(BASE_URL);
    return response.data;
  },

  /**
   * Obtém os detalhes de um projeto específico.
   * @param projectId O ID do projeto.
   * @returns Uma promessa que resolve para o objeto Project.
   */
  getProject: async (projectId: number): Promise<Project> => {
    const response = await apiClient.get<Project>(`${BASE_URL}/${projectId}`);
    return response.data;
  },

  /**
   * Cria um novo projeto.
   * @param data Os dados para criação do projeto.
   * @returns Uma promessa que resolve para o projeto criado.
   */
  createProject: async (data: CreateProjectRequest): Promise<Project> => {
    const response = await apiClient.post<Project>(BASE_URL, data);
    return response.data;
  },

  /**
   * Atualiza um projeto existente.
   * @param projectId O ID do projeto a ser atualizado.
   * @param data Os dados para atualização do projeto.
   * @returns Uma promessa que resolve para o projeto atualizado.
   */
  updateProject: async (projectId: number, data: UpdateProjectRequest): Promise<Project> => {
    const response = await apiClient.put<Project>(`${BASE_URL}/${projectId}`, data);
    return response.data;
  },

  /**
   * Exclui um projeto.
   * @param projectId O ID do projeto a ser excluído.
   * @returns Uma promessa que resolve quando a exclusão for bem-sucedida.
   */
  deleteProject: async (projectId: number): Promise<void> => {
    await apiClient.delete(`${BASE_URL}/${projectId}`);
  },

  /**
   * Faz o upload de imagens para um projeto.
   * @param projectId O ID do projeto.
   * @param files Uma lista de objetos File a serem enviados.
   * @returns Uma promessa que resolve para os metadados das imagens criadas.
   */
  uploadImages: async (projectId: number, files: File[]): Promise<ImageMetadata[]> => {
    const formData = new FormData();
    files.forEach(file => {
      formData.append('files', file);
    });

    const response = await apiClient.post<ImageMetadata[]>(
      `${BASE_URL}/${projectId}/images`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    return response.data;
  },

  /**
   * Obtém uma URL temporária para download de uma imagem.
   * @param projectId O ID do projeto.
   * @param imageId O ID da imagem.
   * @returns Uma promessa que resolve para a URL de download.
   */
  getImageDownloadUrl: async (projectId: number, imageId: number | null): Promise<string> => {

      if (!imageId) {
        // Lança um erro ou retorna uma string vazia, dependendo da sua preferência
        throw new Error("Image ID is required to generate download URL.");
      }
    const response = await apiClient.get<{ downloadUrl: string }>(
      `${BASE_URL}/${projectId}/images/${imageId}/download-url`
    );
    return response.data.downloadUrl;
  },

  /**
   * Exclui uma imagem de um projeto.
   * @param projectId O ID do projeto.
   * @param imageId O ID da imagem a ser excluída.
   * @returns Uma promessa que resolve quando a exclusão for bem-sucedida.
   */
  deleteImage: async (projectId: number, imageId: number): Promise<void> => {
    await apiClient.delete(`${BASE_URL}/${projectId}/images/${imageId}`);
  },
};
