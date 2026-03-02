import { createContext } from 'react';
import { User } from '~/types';

// A interface do tipo do contexto
export interface AuthContextType {
  user: User | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

// O contexto é criado e exportado daqui
export const AuthContext = createContext<AuthContextType | null>(null);