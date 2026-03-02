import React, { useState } from 'react';
import { projectService } from '~/services/projectService';
import { useNavigate } from 'react-router-dom';
import styles from './styles.module.css';

export function ProjectNewPage() {
   const [name, setName] = useState('');
   const [description, setDescription] = useState('');
   const [loading, setLoading] = useState(false);
   const [error, setError] = useState<string | null>(null);
   const navigate = useNavigate();

   const handleSubmit = async (e: React.FormEvent) => {
      e.preventDefault();
      setLoading(true);
      setError(null);

      try {
         const newProject = await projectService.createProject({ name, description });
         alert(`Projeto "${newProject.name}" criado com sucesso!`);
         // Navega para a página de detalhes do projeto recém-criado
         navigate(`/app/projects/${newProject.id}`);
      } catch (err) {
         console.error('Erro ao criar projeto:', err);
         setError('Erro ao criar projeto. Verifique os dados e tente novamente.');
      } finally {
         setLoading(false);
      }
   };

   return (
      <div className={styles.container}>
      <h1 className={styles.title}>Criar Novo Projeto</h1>
      <form onSubmit={handleSubmit} className={styles.form}>
         <div className={styles.formGroup}>
            <label htmlFor="name" className={styles.label}>
               Nome do Projeto:
            </label>
            <input
               id="name"
               type="text"
               value={name}
               onChange={e => setName(e.target.value)}
               required
               disabled={loading}
               className={styles.input}
            />
         </div>

         <div className={styles.formGroup}>
            <label htmlFor="description" className={styles.label}>
               Descrição:
            </label>
            <textarea
               id="description"
               value={description}
               onChange={e => setDescription(e.target.value)}
               disabled={loading}
               className={styles.textarea}
            />
         </div>

         {error && <p className={styles.error}>{error}</p>}

         <button type="submit" disabled={loading} className={styles.submitButton}>
            {loading ? 'Criando...' : 'Criar Projeto'}
         </button>
      </form>
      </div>
   );
}
