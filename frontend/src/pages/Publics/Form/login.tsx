import { FormEvent, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '~/hooks/useAuth';
import { Button } from '~/components/ui/Button';
import { TbMail, TbLock } from 'react-icons/tb';
import styles from './styles.module.css';

export function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLoading(true);
    setError(null);

    const formData = new FormData(event.currentTarget);
    const email = formData.get('email') as string;
    const password = formData.get('password') as string;

    try {
      await login(email, password);
      navigate('/app/home-selection');
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Ocorreu um erro desconhecido.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <form onSubmit={handleSubmit} className={styles.form}>
        {/* Cabeçalho com o Logo */}
        <div className={styles.header}>
          <h1 className={styles.logoGradient}>PIX PRO</h1>
          <p className={styles.title}>Faça login para continuar</p>
        </div>

        {/* Input de Email com Ícone */}
        <div className={styles.inputGroup}>
          <TbMail size={18} className={styles.inputIcon} />
          <input
            name="email"
            type="email"
            placeholder="Email"
            required
            disabled={loading}
            className={styles.inputField}
          />
        </div>

        {/* Input de Senha com Ícone */}
        <div className={styles.inputGroup}>
          <TbLock size={18} className={styles.inputIcon} />
          <input
            name="password"
            type="password"
            placeholder="Senha"
            required
            disabled={loading}
            className={styles.inputField}
          />
        </div>
        
        {error && <p className={styles.error}>{error}</p>}

        {/* Botão com classe para largura total */}
        <Button type="submit" disabled={loading} className={styles.formButton}>
          {loading ? 'Carregando...' : 'Entrar'}
        </Button>

        {/* Link para a página de Cadastro */}
        <p className={styles.toggleLink}>
          Não tem uma conta? <Link to="/register">Cadastre-se</Link>
        </p>
      </form>
    </div>
  );
}