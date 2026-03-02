import { JSX } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '~/hooks/useAuth';

export const ProtectedRoute = ({ children }: { children: JSX.Element }) => {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" />;
  }

  return children;
};