import { Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '~/hooks/useAuth';
import { TbLogout } from 'react-icons/tb';
import styles from './styles.module.css';

export function AuthenticatedLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className={styles.layout}>
      <nav className={styles.navbar}>
        <span className={styles.logo}>PIX PRO</span>
        
        {/* Agrupamos os itens da direita */}
        <div className={styles.navRightItems}>
          <span className={styles.greeting}>Olá, {user?.name?.split(' ')[0]}</span>
          <button onClick={handleLogout} className={styles.logoutButton}>
            {/* Adicionamos o ícone ao lado do texto */}
            <TbLogout size={18} />
            Sair
          </button>
        </div>
      </nav>
      <main className={styles.content}>
        <Outlet /> {/* O conteúdo da rota (ex: Dashboard) será renderizado aqui */}
      </main>
    </div>
  );
}