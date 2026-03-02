import { FormEvent, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register } from '~/services/authService';
import { Button } from '~/components/ui/Button';
// Importa os ícones
import { TbUser, TbMail, TbLock } from 'react-icons/tb';
// Importa os estilos compartilhados
import styles from './styles.module.css'; 

export function RegisterPage() {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setLoading(true);
    setError(null);

    const formData = new FormData(event.currentTarget);
    const name = formData.get('name') as string;
    const email = formData.get('email') as string;
    const password = formData.get('password') as string;
    const confirmPassword = formData.get('confirmPassword') as string;

    if (password !== confirmPassword) {
      setError('As senhas não coincidem.');
      setLoading(false);
      return;
    }

    try {
      await register({ name, email, password });
      alert('Cadastro realizado com sucesso! Faça login para continuar.');
      navigate('/login');
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Ocorreu um erro desconhecido durante o cadastro.');
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
          <p className={styles.title}>Crie sua conta</p>
        </div>

        {/* Input de Nome com Ícone */}
        <div className={styles.inputGroup}>
          <TbUser size={18} className={styles.inputIcon} />
          <input
            name="name"
            type="text"
            placeholder="Nome Completo"
            required
            disabled={loading}
            className={styles.inputField}
          />
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

        {/* Input de Confirmar Senha com Ícone */}
        <div className={styles.inputGroup}>
          <TbLock size={18} className={styles.inputIcon} />
          <input
            name="confirmPassword"
            type="password"
            placeholder="Confirmar Senha"
            required
            disabled={loading}
            className={styles.inputField}
          />
        </div>
        
        {error && <p className={styles.error}>{error}</p>}

        {/* Botão com classe para largura total */}
        <Button type="submit" disabled={loading} className={styles.formButton}>
          {loading ? 'Cadastrando...' : 'Cadastrar'}
        </Button>

        {/* Link para a página de Login */}
        <p className={styles.toggleLink}>
          Já tem conta? <Link to="/login">Faça Login</Link>
        </p>
      </form>
    </div>
  );
}