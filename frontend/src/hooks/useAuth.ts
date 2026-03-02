import { useContext } from 'react';
import { AuthContext, AuthContextType } from '~/contexts/AuthContext';

export const useAuth = () => {
  const context = useContext(AuthContext as React.Context<AuthContextType>); 
  if (!context) {
    throw new Error('useAuth deve ser usado dentro de um AuthProvider');
  }
  return context;
};