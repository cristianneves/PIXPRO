import { useState, useEffect } from 'react';
import { Project } from '~/types';
import { projectService } from '~/services/projectService';
import styles from './styles.module.css';
import { Link } from 'react-router-dom';
import { TbPhoto, TbPhotoOff, TbPhotoScan } from 'react-icons/tb';

interface ProjectCardProps {
  project: Project;
}

export function ProjectCard({ project }: ProjectCardProps) {
  const [imageUrl, setImageUrl] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  const imageCount = project.images?.length || 0;
  const thumbnailId = (project.images && project.images.length > 0)
    ? project.images[0].id
    : null;

  useEffect(() => {
    if (!thumbnailId) {
      setLoading(false);
      return;
    }

    const fetchImageUrl = async () => {
      try {
        const url = await projectService.getImageDownloadUrl(
          project.id,
          thumbnailId
        );
        setImageUrl(url);
      } catch (error) {
        console.error('Erro ao buscar URL da imagem:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchImageUrl();
  }, [project.id, thumbnailId]);

  // Função para renderizar o placeholder de forma inteligente
  const renderPlaceholder = () => {
    if (loading) {
      return (
        <div className={`${styles.placeholder} ${styles.loading}`}>
          <TbPhotoScan size={30} />
          <span>Carregando...</span>
        </div>
      );
    }
    // Se não está carregando e não tem thumbnailId
    return (
      <div className={styles.placeholder}>
        <TbPhotoOff size={30} />
        <span>Sem Imagens</span>
      </div>
    );
  };

  return (
    // O Link agora é uma classe separada para não ter sublinhado
    <Link to={`/app/projects/${project.id}`} className={styles.cardLink}>
      <div className={styles.card}>
        <div className={styles.thumbnailContainer}>
          {imageUrl ? (
            <img src={imageUrl} alt={project.name} className={styles.thumbnail} />
          ) : (
            renderPlaceholder()
          )}
        </div>
        <div className={styles.cardFooter}>
          <p className={styles.projectName}>{project.name}</p>
          <span className={styles.imageCount}>
            <TbPhoto size={16} />
            {imageCount} {imageCount === 1 ? 'imagem' : 'imagens'}
          </span>
        </div>
      </div>
    </Link>
  );
}