import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Project, ImageMetadata } from '~/types';
import { projectService } from '~/services/projectService';
import { Button } from '~/components/ui/Button';
import { TbUpload, TbPhoto } from 'react-icons/tb';
import styles from './styles.module.css';

// COMPONENTE THUMBNAIL
function ProjectImageThumbnail({
  projectId,
  image,
  onDelete,
  onDownload,
}: {
  projectId: number;
  image: ImageMetadata;
  onDelete: (imageId: number) => void;
  onDownload: (image: ImageMetadata) => void;
}) {
  const [imageUrl, setImageUrl] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (image.status === 'COMPLETED') {
      const fetchUrl = async () => {
        try {
          setIsLoading(true);
          const url = await projectService.getImageDownloadUrl(projectId, image.id);
          setImageUrl(url);
        } catch (error) {
          console.error(`Erro ao buscar URL para imagem ${image.id}:`, error);
        } finally {
          setIsLoading(false);
        }
      };
      fetchUrl();
    } else {
      setIsLoading(false);
    }
  }, [projectId, image.id, image.status]);

  return (
    <div className={styles.imageCard}>
      <div className={styles.imageContainer}>
        {isLoading && <span>Carregando...</span>}
        {!isLoading && image.status !== 'COMPLETED' && <span>Processando...</span>}
        {!isLoading && imageUrl && (
          <img src={imageUrl} alt={image.fileName} className={styles.image} />
        )}
      </div>

      <div className={styles.cardContent}>
        <p className={styles.fileName}>{image.fileName}</p>
        <div className={styles.cardActions}>
          <Button
            onClick={() => onDownload(image)}
            disabled={image.status !== 'COMPLETED'}
            variant="secondary"
          >
            Download
          </Button>
          <Button onClick={() => onDelete(image.id)} variant="destructive">
            Excluir
          </Button>
        </div>
      </div>
    </div>
  );
}

// COMPONENTE PRINCIPAL DA PÁGINA
export function ProjectDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const projectId = Number(id);
  const navigate = useNavigate();

  const [project, setProject] = useState<Project | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isUploading, setIsUploading] = useState(false);

  const fetchProject = useCallback(async () => {
    if (!projectId) return;
    setLoading(true);
    try {
      const data = await projectService.getProject(projectId);
      setProject(data);
    } catch (err) {
      console.error('Erro ao buscar projeto:', err);
      setError('Projeto não encontrado ou erro ao carregar.');
    } finally {
      setLoading(false);
    }
  }, [projectId]);

  useEffect(() => {
    fetchProject();
  }, [fetchProject]);

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || !projectId) return;
    const files = Array.from(e.target.files);
    if (files.length === 0) return;
    
    setIsUploading(true);
    try {
      const newImages = await projectService.uploadImages(projectId, files);
      alert(`${newImages.length} imagens enviadas com sucesso!`);
      setProject(prev => (prev ? { ...prev, images: [...prev.images, ...newImages] } : null));
    } catch (err) {
      console.error('Erro ao fazer upload:', err);
      alert('Erro ao fazer upload das imagens.');
    } finally {
      setIsUploading(false);
      e.target.value = '';
    }
  };

  const handleDeleteImage = async (imageId: number) => {
    if (!window.confirm('Tem certeza que deseja excluir esta imagem?')) return;
    try {
      await projectService.deleteImage(projectId, imageId);
      alert('Imagem excluída com sucesso!');
      setProject(prev =>
        prev ? { ...prev, images: prev.images.filter(img => img.id !== imageId) } : null
      );
    } catch (err) {
      console.error('Erro ao excluir imagem:', err);
      alert('Erro ao excluir imagem.');
    }
  };

  const handleDownloadImage = async (image: ImageMetadata) => {
    try {
      // Obtém a URL pública/assinada da imagem
      const url = await projectService.getImageDownloadUrl(projectId, image.id);
      
      // Busca os dados da imagem como um "Blob" (arquivo bruto)
      const response = await fetch(url);
      const blob = await response.blob();
      
      // Cria uma URL temporária local para esse Blob
      const blobUrl = window.URL.createObjectURL(blob);
      
      // Cria o link invisível para forçar o download
      const link = document.createElement('a');
      link.href = blobUrl;
      link.setAttribute('download', image.fileName);
      document.body.appendChild(link);
      
      // Clica e limpa
      link.click();
      
      // Remove o link e libera a memória do Blob
      link.parentNode?.removeChild(link);
      window.URL.revokeObjectURL(blobUrl);

    } catch (err) {
      console.error('Erro ao baixar imagem:', err);
      alert('Não foi possível fazer o download da imagem. Verifique se o servidor permite acesso externo (CORS).');
    }
  };

  const handleDeleteProject = async () => {
    if (!window.confirm(`Tem certeza que deseja excluir o projeto "${project?.name}"?`)) return;
    try {
      await projectService.deleteProject(projectId);
      alert('Projeto excluído com sucesso!');
      navigate('/app/dashboard');
    } catch (err) {
      console.error('Erro ao excluir projeto:', err);
      alert('Erro ao excluir o projeto. Tente novamente.');
    }
  };

  if (loading) {
    return <div>Carregando detalhes do projeto...</div>;
  }

  if (error || !project) {
    return <div>{error || 'Projeto não encontrado.'}</div>;
  }

  return (
    <div className={styles.pageWrapper}>
      <header className={styles.pageHeader}>
        <h1 className={styles.title}>{project.name}</h1>
        <Button onClick={() => navigate('/app/dashboard')} variant="secondary">
          ← Voltar para o Dashboard
        </Button>
      </header>

      <div className={styles.metaInfo}>
        <p>Descrição: {project.description}</p>
        <p>Criado em: {new Date(project.createdAt).toLocaleDateString()}</p>
      </div>

      <section className={styles.uploadSection}>
        <h2><TbUpload /> Upload de Imagens</h2>
        <label className={styles.uploadLabel}>
          <TbPhoto size={40} style={{ marginBottom: '1rem' }} />
          {isUploading ? 'Enviando imagens...' : 'Arraste ou clique para selecionar os arquivos'}
          <input
            type="file"
            multiple
            onChange={handleFileUpload}
            disabled={isUploading}
            className={styles.fileInput}
          />
        </label>
      </section>

      <section>
        <h2>Imagens ({project.images.length})</h2>
        {project.images.length === 0 ? (
          <p className={styles.noImagesText}>Nenhuma imagem neste projeto.</p>
        ) : (
          <div className={styles.imageGrid}>
            {project.images.map(image => (
              <ProjectImageThumbnail
                key={image.id}
                projectId={project.id}
                image={image}
                onDelete={handleDeleteImage}
                onDownload={handleDownloadImage}
              />
            ))}
          </div>
        )}
      </section>

      <section className={styles.dangerZone}>
        <h3>Zona de Perigo</h3>
        <p>
          A exclusão de um projeto é permanente e não pode ser desfeita. Todas as
          imagens associadas também serão perdidas.
        </p>
        <Button onClick={handleDeleteProject} variant="destructive">
          Excluir este Projeto Permanentemente
        </Button>
      </section>
    </div>
  );
}