import { Link } from 'react-router-dom';
import { Button } from '~/components/ui/Button';
import styles from './styles.module.css';
import { TbArrowRight, TbSparkles, TbDeviceDesktop, TbStack } from 'react-icons/tb';

export function LandingPage() {
  return (
    <div className={styles.pageContainer}>
      {/* O Novo Hero Section */}
      <header className={styles.heroSection}>
        {/* O logo agora usa o gradiente que definimos */}
        <h1 className={styles.logoGradient}>PIX PRO</h1>
        <p className={styles.slogan}>
          A plataforma inteligente de processamento de imagens.
        </p>
        <p className={styles.subSlogan}>
          Automatize, melhore e transforme suas imagens em segundos com o poder da IA.
        </p>
        
        {/* Usa os componentes de Botão (Primary e Secondary) */}
        <div className={styles.actions}>
          <Link to="/register">
            <Button variant="primary" className={styles.ctaButton}>
              Começar Agora
              <TbArrowRight size={20} />
            </Button>
          </Link>
          <Link to="/login">
            <Button variant="secondary">
              Fazer Login
            </Button>
          </Link>
        </div>
      </header>

      {/* Seção de Features Atualizada */}
      <main className={styles.features}>
        <div className={styles.featureCard}>
          <TbSparkles size={30} className={styles.featureIcon} />
          <h3>Processamento com IA</h3>
          <p>Aplique melhorias automáticas, filtros artísticos e detecção de objetos com um clique.</p>
        </div>
        <div className={styles.featureCard}>
          <TbStack size={30} className={styles.featureIcon} />
          <h3>Gestão em Lote</h3>
          <p>Faça upload e processe centenas de imagens de uma vez. Perfeito para grandes volumes.</p>
        </div>
        <div className={styles.featureCard}>
          <TbDeviceDesktop size={30} className={styles.featureIcon} />
          <h3>Plataforma Web</h3>
          <p>Acesse de qualquer lugar. Seus projetos e imagens sincronizados na nuvem.</p>
        </div>
      </main>

      {/* Rodapé Simples */}
      <footer className={styles.footer}>
        <p>© 2025 PixPro. Todos os direitos reservados.</p>
      </footer>
    </div>
  );
}