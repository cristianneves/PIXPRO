import axios from 'axios';

// O endereço do API Gateway
const API_BASE_URL = 'http://localhost:8080'; 

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
});

// Interceptor: Adiciona o token JWT a CADA requisição autenticada
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);