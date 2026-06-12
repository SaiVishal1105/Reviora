import React, { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Palette, Check } from 'lucide-react'
import { useThemeStore, THEMES, type Theme } from '@/store/themeStore'

export const ThemeSelector: React.FC = () => {
  const { theme, setTheme } = useThemeStore()
  const [open, setOpen] = useState(false)

  return (
    <div className="relative">
      <button
        onClick={() => setOpen(!open)}
        className="flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg text-sm transition-all"
        style={{
          color: 'var(--text-secondary)',
          background: open ? 'var(--bg-hover)' : 'transparent',
        }}
        title="Change theme"
      >
        <Palette size={15} />
        <span className="hidden sm:block text-xs" style={{ fontFamily: 'var(--font-mono)' }}>
          {THEMES.find((t) => t.id === theme)?.name}
        </span>
      </button>

      <AnimatePresence>
        {open && (
          <>
            <div className="fixed inset-0 z-40" onClick={() => setOpen(false)} />
            <motion.div
              initial={{ opacity: 0, y: 8, scale: 0.95 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 8, scale: 0.95 }}
              transition={{ duration: 0.15 }}
              className="absolute right-0 top-full mt-2 z-50 rounded-xl border p-2 min-w-48 shadow-xl"
              style={{
                background: 'var(--bg-card)',
                borderColor: 'var(--border)',
                boxShadow: '0 8px 32px rgba(0,0,0,0.3)',
              }}
            >
              <p
                className="text-xs px-2 pb-2 mb-1 border-b"
                style={{ color: 'var(--text-muted)', borderColor: 'var(--border)', fontFamily: 'var(--font-mono)' }}
              >
                SELECT THEME
              </p>
              {THEMES.map((t) => (
                <button
                  key={t.id}
                  onClick={() => { setTheme(t.id as Theme); setOpen(false) }}
                  className="w-full flex items-center gap-3 px-2 py-2 rounded-lg transition-all text-left"
                  style={{
                    background: theme === t.id ? 'var(--accent-muted)' : 'transparent',
                    color: theme === t.id ? 'var(--accent)' : 'var(--text-primary)',
                  }}
                >
                  <div className="flex gap-1">
                    {t.preview.map((color, i) => (
                      <div
                        key={i}
                        className="w-3 h-3 rounded-full border border-white/10"
                        style={{ background: color }}
                      />
                    ))}
                  </div>
                  <div className="flex-1">
                    <p className="text-xs font-semibold">{t.name}</p>
                    <p className="text-xs" style={{ color: 'var(--text-muted)' }}>
                      {t.description}
                    </p>
                  </div>
                  {theme === t.id && <Check size={12} style={{ color: 'var(--accent)' }} />}
                </button>
              ))}
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </div>
  )
}
