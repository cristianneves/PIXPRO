import { useEffect, useState } from 'react';
import { Project } from '~/types';
import { projectService } from '~/services/projectService';
import { Link } from 'react-router-dom';

export function ProjectListPage() {
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
        setError('Não foi possível carregar a lista de projetos.');
      } finally {
        setLoading(false);
      }
    };

    fetchProjects();
  }, []);

  if (loading) {
    return <div>Carregando projetos...</div>;
  }

  if (error) {
    return <div>Erro: {error}</div>;
  }

  return (
    <div>
      <h1>Meus Projetos</h1>
      <Link to="/app/projects/new">
        <button>Novo Projeto</button>
      </Link>

      {projects.length === 0 ? (
        <p>Você não tem projetos. Crie um novo para começar!</p>
      ) : (
        <ul>
          {projects.map(project => (
            <li key={project.id}>
              <Link to={`/app/projects/${project.id}`}>
                <h2>{project.name}</h2>
              </Link>
              <p>{project.description}</p>
              <p>Imagens: {project.images.length}</p>
              {/* Aqui você pode adicionar botões de Editar e Excluir */}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
