import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { projectService } from '~/services/projectService';
import { Project } from '~/types';
import { ProjectCard } from '~/components/ui/ProjectCard';
import { Button } from '~/components/ui/Button';
import { TbPlus, TbLayoutGrid } from 'react-icons/tb';
import styles from './styles.module.css';

export function DashboardPage() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchProjects = async () => {
      try {
        const data = await projectService.listProjects();
        setProjects(data);
      } catch (err) {
        console.error('Erro ao buscar projetos:', err);
        setError('Não foi possível carregar seus projetos. Tente novamente mais tarde.');
      } finally {
        setLoading(false);
      }
    };

    fetchProjects();
  }, []);

  // --- Renderização de Estados ---
  if (loading) {
    return (
      <div className={styles.pageWrapper}>
        <div className={styles.pageHeader}>
          <h2 className={styles.pageTitle}>Meus Projetos</h2>
        </div>
        <p className={styles.loadingState}>Carregando projetos...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className={styles.pageWrapper}>
        <div className={styles.pageHeader}>
          <h2 className={styles.pageTitle}>Meus Projetos</h2>
        </div>
        <p className={styles.errorState}>{error}</p>
      </div>
    );
  }

  // --- Renderização Principal ---
  return (
    <div className={styles.pageWrapper}>
      {/* O novo cabeçalho com o botão de CTA primário */}
      <div className={styles.pageHeader}>
        <h2 className={styles.pageTitle}>Meus Projetos</h2>
        <Link to="/app/projects/new">
          <Button>
            <TbPlus size={18} />
            Criar Novo Projeto
          </Button>
        </Link>
      </div>

      {/* Grid de Projetos */}
      <div className={styles.projectGrid}>
        {/* Mapeia os projetos existentes */}
        {projects.map(project => (
          <ProjectCard key={project.id} project={project} />
        ))}
      </div>

      {/* Mensagem de Estado Vazio melhorada */}
      {projects.length === 0 && (
        <div className={styles.emptyState}>
          <TbLayoutGrid size={40} />
          <p>Você ainda não tem projetos.</p>
          <span>Clique em "Criar Novo Projeto" para começar.</span>
        </div>
      )}
    </div>
  );
}