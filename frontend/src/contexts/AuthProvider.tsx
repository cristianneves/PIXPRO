import { useState, ReactNode, useEffect } from 'react';
import { User } from '~/types';
import * as authService from '~/services/authService';
// Importa o contexto do novo arquivo
import { AuthContext } from './AuthContext';

export default function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const user = authService.getCurrentUser();
    if (user) {
      setUser(user);
    }
    setLoading(false);
  }, []);

  const login = async (email: string, password: string) => {
    const userData = await authService.login(email, password);
    setUser(userData);
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  if (loading) {
    return null; // Ou um spinner
  }

  return (
    // Usa o AuthContext importado
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}