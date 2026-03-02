import { apiClient } from './apiClient';
import { jwtDecode } from 'jwt-decode';
import { User } from '~/types';
import { RegisterRequest } from '~/types';

interface LoginResponse {
  token: string;
}

interface DecodedToken {
  userId: string;
  sub: string; 
  roles: string[];
  name: string;
  iat: number;
  exp: number;
}

const TOKEN_KEY = 'authToken';

export const register = async (data: RegisterRequest): Promise<void> => {
  try {
    await apiClient.post('/api/auth/register', data);
  } catch (error) {
    console.error('Falha no cadastro:', error);
    throw new Error('Erro ao tentar cadastrar. Verifique os dados.');
  }
};

export const login = async (email: string, password: string): Promise<User> => {
  try {
    const response = await apiClient.post<LoginResponse>('/api/auth/login', {
      email,
      password,
    });

    const { token } = response.data;
    localStorage.setItem(TOKEN_KEY, token);
    const decodedToken: DecodedToken = jwtDecode(token);
    
    return {
      id: decodedToken.userId,
      name: decodedToken.name,
      email: decodedToken.sub,
      roles: decodedToken.roles,
    };

  } catch (error) { // Este 'error' ESTÁ sendo usado na linha abaixo
    console.error('Falha no login:', error);
    throw new Error('Email ou senha inválidos.');
  }
};

export const logout = () => {
  localStorage.removeItem(TOKEN_KEY);
};

export const getCurrentUser = (): User | null => {
  try {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) {
      return null;
    }

    const decodedToken: DecodedToken = jwtDecode(token);
    if (decodedToken.exp * 1000 < Date.now()) {
      localStorage.removeItem(TOKEN_KEY);
      return null;
    }

    return {
      id: decodedToken.userId,
      name: decodedToken.name,
      email: decodedToken.sub,
      roles: decodedToken.roles,
    };
  } catch (error) {
    // Token inválido ou corrompido, mas não precisamos usar o 'error'
    localStorage.removeItem(TOKEN_KEY);
    console.error('Erro ao decodificar o token:', error);
    return null;
  }
};