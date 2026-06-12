import React from 'react'
import { Loader2 } from 'lucide-react'
import { clsx } from 'clsx'

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'ghost' | 'danger' | 'success'
  size?: 'sm' | 'md' | 'lg'
  loading?: boolean
  icon?: React.ReactNode
  iconPosition?: 'left' | 'right'
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  loading = false,
  icon,
  iconPosition = 'left',
  children,
  className,
  disabled,
  onClick,
  ...props
}) => {
  const baseStyles =
    'inline-flex items-center justify-center gap-2 font-medium rounded-lg transition-all duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed select-none active:scale-95'

  const variants = {
    primary:  'bg-[var(--accent)] hover:bg-[var(--accent-hover)] text-white',
    secondary:'bg-[var(--bg-card)] border border-[var(--border)] text-[var(--text-primary)] hover:border-[var(--text-muted)] hover:bg-[var(--bg-hover)]',
    ghost:    'bg-transparent text-[var(--text-secondary)] hover:text-[var(--text-primary)] hover:bg-[var(--bg-hover)]',
    danger:   'bg-[var(--error)] hover:opacity-90 text-white',
    success:  'bg-[var(--success)] hover:opacity-90 text-black',
  }

  const sizes = {
    sm: 'h-8 px-3 text-xs gap-1.5',
    md: 'h-9 px-4 text-sm',
    lg: 'h-11 px-6 text-base',
  }

  return (
    <button
      className={clsx(baseStyles, variants[variant], sizes[size], className)}
      disabled={disabled || loading}
      onClick={onClick}
      style={{ fontFamily: 'var(--font-sans)' }}
      {...props}
    >
      {loading ? (
        <Loader2 size={size === 'sm' ? 12 : size === 'lg' ? 18 : 14} className="animate-spin" />
      ) : (
        icon && iconPosition === 'left' && <span className="flex-shrink-0">{icon}</span>
      )}
      {children}
      {!loading && icon && iconPosition === 'right' && (
        <span className="flex-shrink-0">{icon}</span>
      )}
    </button>
  )
}
