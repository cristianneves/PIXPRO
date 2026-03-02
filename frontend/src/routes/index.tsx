import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { DashboardPage } from '~/pages/Authenticated/Dashboard';
import { LoginPage } from '~/pages/Publics/Form/login';
import { RegisterPage } from '~/pages/Publics/Form/cadastro';
import { LandingPage } from '~/pages/Publics/LandingPage';
import { AuthenticatedLayout } from '~/components/layout/AuthenticatedLayout';
import { ProjectListPage } from '~/pages/Authenticated/Projects/List';
import { ProjectNewPage } from '~/pages/Authenticated/Projects/New';
import { ProjectDetailsPage } from '~/pages/Authenticated/Projects/Details';
import { HomeSelectionPage } from '~/pages/Authenticated/HomeSelection';
import { ObjectDetectionPage } from '~/pages/Authenticated/ObjectDetection';

export function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Rotas Públicas */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Rotas Protegidas */}
        <Route
          path="/app"
          element={
            <ProtectedRoute>
              <AuthenticatedLayout />
            </ProtectedRoute>
          }
        >
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="projects" element={<ProjectListPage />} />
          <Route path="projects/new" element={<ProjectNewPage />} />
          <Route path="projects/:id" element={<ProjectDetailsPage />} />
          <Route path="home-selection" element={<HomeSelectionPage />} />
          <Route path="detection" element={<ObjectDetectionPage />} />
          {/* Adicione outras rotas do app aqui (ex: app/projetos, app/upload) */}
        </Route>
      </Routes>
    </BrowserRouter>
  );
}