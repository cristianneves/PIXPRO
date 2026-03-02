import { Link } from 'react-router-dom';
import { useAuth } from '~/hooks/useAuth';
import styles from './styles.module.css';
// Importa ícones para as opções
import { TbPhotoEdit, TbScanEye } from 'react-icons/tb'; 

export function HomeSelectionPage() {
  const { user } = useAuth();
  // Pega o primeiro nome do usuário para a saudação
  const firstName = user?.name?.split(' ')[0];

  return (
    <div className={styles.pageWrapper}>
      {/* Cabeçalho de Boas-vindas */}
      <header className={styles.header}>
        <h1 className={styles.title}>Bem-vindo, {firstName}!</h1>
        <p className={styles.subtitle}>O que você gostaria de fazer hoje?</p>
      </header>

      {/* Grid com as opções de módulo */}
      <main className={styles.selectionGrid}>

        {/* Edição de Imagem (leva ao Dashboard) */}
        <Link to="/app/dashboard" className={styles.optionCard}>
          <div className={styles.iconWrapper} style={{ color: 'var(--accent-blue)' }}>
            <TbPhotoEdit size={40} />
          </div>
          <h2 className={styles.optionTitle}>Edição de Imagem</h2>
          <p className={styles.optionDescription}>
            Gerencie seus projetos, faça uploads em lote e aplique melhorias.
          </p>
          <span className={styles.goArrow}>→</span>
        </Link>

        {/* Detecção de Objetos */}
        <Link to="/app/detection" className={`${styles.optionCard}`}>
          <div className={styles.iconWrapper}>
            <TbScanEye size={40} />
          </div>
          <h2 className={styles.optionTitle}>Detecção de Objetos</h2>
          <p className={styles.optionDescription}>
            Analise imagens para identificar e localizar objetos específicos.
          </p>
          <span className={styles.goArrow}>→</span>
        </Link>

      </main>
    </div>
  );
}