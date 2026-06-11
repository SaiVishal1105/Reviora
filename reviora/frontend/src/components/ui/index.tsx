import React from 'react'
import { clsx } from 'clsx'

// ─── Badge ───────────────────────────────────────────
interface BadgeProps {
  children: React.ReactNode
  variant?: 'default' | 'success' | 'error' | 'warning' | 'info' | 'accent'
  size?: 'sm' | 'md'
  className?: string
}

export const Badge: React.FC<BadgeProps> = ({
  children,
  variant = 'default',
  size = 'sm',
  className,
}) => {
  const variants = {
    default: 'bg-[var(--bg-tertiary)] text-[var(--text-secondary)] border-[var(--border)]',
    success: 'bg-[var(--success)]/15 text-[var(--success)] border-[var(--success)]/30',
    error: 'bg-[var(--error)]/15 text-[var(--error)] border-[var(--error)]/30',
    warning: 'bg-[var(--warning)]/15 text-[var(--warning)] border-[var(--warning)]/30',
    info: 'bg-[var(--info)]/15 text-[var(--info)] border-[var(--info)]/30',
    accent: 'bg-[var(--accent-muted)] text-[var(--accent)] border-[var(--accent)]/30',
  }

  const sizes = {
    sm: 'text-xs px-2 py-0.5',
    md: 'text-sm px-3 py-1',
  }

  return (
    <span
      className={clsx(
        'inline-flex items-center gap-1 rounded-full border font-mono font-medium',
        variants[variant],
        sizes[size],
        className
      )}
    >
      {children}
    </span>
  )
}

// ─── Skeleton ────────────────────────────────────────
interface SkeletonProps {
  className?: string
  lines?: number
}

export const Skeleton: React.FC<SkeletonProps> = ({ className, lines = 1 }) => {
  if (lines > 1) {
    return (
      <div className="space-y-2">
        {Array.from({ length: lines }).map((_, i) => (
          <div
            key={i}
            className={clsx(
              'shimmer rounded',
              i === lines - 1 ? 'w-2/3' : 'w-full',
              'h-4',
              className
            )}
          />
        ))}
      </div>
    )
  }
  return <div className={clsx('shimmer rounded', className)} />
}

// ─── Panel Card ──────────────────────────────────────
interface PanelProps {
  title?: string
  subtitle?: string
  actions?: React.ReactNode
  children: React.ReactNode
  className?: string
  noPadding?: boolean
}

export const Panel: React.FC<PanelProps> = ({
  title,
  subtitle,
  actions,
  children,
  className,
  noPadding = false,
}) => {
  return (
    <div
      className={clsx(
        'rounded-lg border flex flex-col',
        className
      )}
      style={{
        background: 'var(--bg-card)',
        borderColor: 'var(--border)',
      }}
    >
      {(title || actions) && (
        <div
          className="flex items-center justify-between px-4 py-3 border-b flex-shrink-0"
          style={{ borderColor: 'var(--border)' }}
        >
          <div>
            {title && (
              <h3
                className="text-sm font-semibold tracking-tight"
                style={{ color: 'var(--text-primary)', fontFamily: 'var(--font-display)' }}
              >
                {title}
              </h3>
            )}
            {subtitle && (
              <p className="text-xs mt-0.5" style={{ color: 'var(--text-muted)' }}>
                {subtitle}
              </p>
            )}
          </div>
          {actions && <div className="flex items-center gap-2">{actions}</div>}
        </div>
      )}
      <div className={clsx('flex-1 overflow-auto', !noPadding && 'p-4')}>{children}</div>
    </div>
  )
}

// ─── Select ──────────────────────────────────────────
interface SelectOption {
  value: string
  label: string
}

interface SelectProps {
  value: string
  onChange: (value: string) => void
  options: SelectOption[]
  className?: string
}

export const Select: React.FC<SelectProps> = ({ value, onChange, options, className }) => {
  return (
    <select
      value={value}
      onChange={(e) => onChange(e.target.value)}
      className={clsx('text-sm rounded-lg px-3 py-1.5 border cursor-pointer focus:outline-none', className)}
      style={{
        background: 'var(--bg-tertiary)',
        borderColor: 'var(--border)',
        color: 'var(--text-primary)',
        fontFamily: 'var(--font-mono)',
      }}
    >
      {options.map((opt) => (
        <option key={opt.value} value={opt.value}>
          {opt.label}
        </option>
      ))}
    </select>
  )
}

// ─── Input ───────────────────────────────────────────
interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
  icon?: React.ReactNode
}

export const Input: React.FC<InputProps> = ({ label, error, icon, className, ...props }) => {
  return (
    <div className="flex flex-col gap-1.5">
      {label && (
        <label className="text-sm font-medium" style={{ color: 'var(--text-secondary)' }}>
          {label}
        </label>
      )}
      <div className="relative">
        {icon && (
          <span className="absolute left-3 top-1/2 -translate-y-1/2" style={{ color: 'var(--text-muted)' }}>
            {icon}
          </span>
        )}
        <input
          className={clsx(
            'w-full rounded-lg border px-3 py-2 text-sm transition-colors focus:outline-none',
            icon && 'pl-9',
            error ? 'border-[var(--error)]' : 'border-[var(--border)] focus:border-[var(--accent)]',
            className
          )}
          style={{
            background: 'var(--bg-tertiary)',
            color: 'var(--text-primary)',
            fontFamily: 'var(--font-sans)',
          }}
          {...props}
        />
      </div>
      {error && (
        <p className="text-xs" style={{ color: 'var(--error)' }}>
          {error}
        </p>
      )}
    </div>
  )
}
