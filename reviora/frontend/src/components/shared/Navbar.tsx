import React from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { motion } from 'framer-motion'
import {
  Code2, BarChart3, User, LogOut, Settings, ChevronDown, Zap,
} from 'lucide-react'
import { useAuthStore } from '@/store/authStore'
import { ThemeSelector } from './ThemeSelector'
import { useState } from 'react'
import { AnimatePresence } from 'framer-motion'

export const Navbar: React.FC = () => {
  const { user, isAuthenticated, clearAuth } = useAuthStore()
  const navigate = useNavigate()
  const location = useLocation()
  const [userMenuOpen, setUserMenuOpen] = useState(false)

  const handleLogout = () => {
    clearAuth()
    navigate('/login')
  }

  const navLinks = [
    { to: '/workspace', label: 'Workspace', icon: <Code2 size={14} /> },
    { to: '/dashboard', label: 'Dashboard', icon: <BarChart3 size={14} /> },
  ]

  return (
    <header
      className="h-12 flex items-center px-4 border-b flex-shrink-0 relative z-30"
      style={{
        background: 'var(--bg-secondary)',
        borderColor: 'var(--border)',
      }}
    >
      {/* Logo */}
      <Link to="/" className="flex items-center gap-2 mr-6">
        <div
          className="w-7 h-7 rounded-lg flex items-center justify-center"
          style={{ background: 'var(--accent)' }}
        >
          <Zap size={14} className="text-white" />
        </div>
        <span
          className="text-base font-bold tracking-tight"
          style={{ fontFamily: 'var(--font-display)', color: 'var(--text-primary)' }}
        >
          Reviora
        </span>
      </Link>

      {/* Nav links */}
      {isAuthenticated && (
        <nav className="flex items-center gap-1 mr-auto">
          {navLinks.map((link) => {
            const active = location.pathname.startsWith(link.to)
            return (
              <Link
                key={link.to}
                to={link.to}
                className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-all relative"
                style={{
                  color: active ? 'var(--accent)' : 'var(--text-secondary)',
                  background: active ? 'var(--accent-muted)' : 'transparent',
                }}
              >
                {link.icon}
                {link.label}
              </Link>
            )
          })}
        </nav>
      )}

      {/* Right side */}
      <div className="flex items-center gap-2 ml-auto">
        <ThemeSelector />

        {isAuthenticated ? (
          <div className="relative">
            <button
              onClick={() => setUserMenuOpen(!userMenuOpen)}
              className="flex items-center gap-2 px-2 py-1.5 rounded-lg transition-all"
              style={{ color: 'var(--text-secondary)' }}
            >
              {user?.picture ? (
                <img src={user.picture} alt={user.name} className="w-6 h-6 rounded-full" />
              ) : (
                <div
                  className="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold"
                  style={{ background: 'var(--accent-muted)', color: 'var(--accent)' }}
                >
                  {user?.name?.[0]?.toUpperCase()}
                </div>
              )}
              <span className="text-xs font-medium hidden sm:block" style={{ color: 'var(--text-primary)' }}>
                {user?.name?.split(' ')[0]}
              </span>
              <ChevronDown size={12} />
            </button>

            <AnimatePresence>
              {userMenuOpen && (
                <>
                  <div className="fixed inset-0 z-40" onClick={() => setUserMenuOpen(false)} />
                  <motion.div
                    initial={{ opacity: 0, y: 8, scale: 0.95 }}
                    animate={{ opacity: 1, y: 0, scale: 1 }}
                    exit={{ opacity: 0, y: 8, scale: 0.95 }}
                    transition={{ duration: 0.15 }}
                    className="absolute right-0 top-full mt-2 z-50 rounded-xl border p-1.5 min-w-44 shadow-xl"
                    style={{
                      background: 'var(--bg-card)',
                      borderColor: 'var(--border)',
                      boxShadow: '0 8px 32px rgba(0,0,0,0.3)',
                    }}
                  >
                    <div className="px-3 py-2 border-b mb-1" style={{ borderColor: 'var(--border)' }}>
                      <p className="text-xs font-semibold" style={{ color: 'var(--text-primary)' }}>
                        {user?.name}
                      </p>
                      <p className="text-xs" style={{ color: 'var(--text-muted)' }}>
                        {user?.email}
                      </p>
                    </div>
                    {[
                      { icon: <User size={13} />, label: 'Profile', action: () => { navigate('/profile'); setUserMenuOpen(false) } },
                      { icon: <Settings size={13} />, label: 'Settings', action: () => { navigate('/settings'); setUserMenuOpen(false) } },
                      { icon: <LogOut size={13} />, label: 'Sign out', action: handleLogout, danger: true },
                    ].map((item) => (
                      <button
                        key={item.label}
                        onClick={item.action}
                        className="w-full flex items-center gap-2.5 px-3 py-2 rounded-lg text-xs transition-all text-left"
                        style={{
                          color: item.danger ? 'var(--error)' : 'var(--text-secondary)',
                        }}
                        onMouseEnter={(e) => {
                          e.currentTarget.style.background = 'var(--bg-hover)'
                          e.currentTarget.style.color = item.danger ? 'var(--error)' : 'var(--text-primary)'
                        }}
                        onMouseLeave={(e) => {
                          e.currentTarget.style.background = 'transparent'
                          e.currentTarget.style.color = item.danger ? 'var(--error)' : 'var(--text-secondary)'
                        }}
                      >
                        {item.icon}
                        {item.label}
                      </button>
                    ))}
                  </motion.div>
                </>
              )}
            </AnimatePresence>
          </div>
        ) : (
          <div className="flex items-center gap-2">
            <Link
              to="/login"
              className="px-3 py-1.5 rounded-lg text-xs font-medium transition-all"
              style={{ color: 'var(--text-secondary)' }}
            >
              Sign in
            </Link>
            <Link
              to="/register"
              className="px-3 py-1.5 rounded-lg text-xs font-semibold transition-all"
              style={{ background: 'var(--accent)', color: 'white' }}
            >
              Get started
            </Link>
          </div>
        )}
      </div>
    </header>
  )
}
