import styles from './styles.module.css';

type ButtonVariant = 'primary' | 'secondary' | 'destructive';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children: React.ReactNode;
  variant?: ButtonVariant;
  className?: string;
}

export const Button = ({
  children,
  variant = 'primary',
  className,
  ...props
}: ButtonProps) => {

  // Constrói a lista de classes dinamicamente
  // Isso aplica a classe base '.button', a classe da variante (ex: '.primary'),
  // e qualquer outra classe que for passar (ex: className="login-button")
  const buttonClasses = [
    styles.button,
    styles[variant],
    className
  ].filter(Boolean).join(' ');

  return (
    <button className={buttonClasses} {...props}>
      {children}
    </button>
  );
};