import React, { Suspense, lazy } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import { AppLayout, PublicLayout } from '@/layouts/AppLayout'
import { ProtectedRoute } from '@/components/shared/ProtectedRoute'

const HomePage      = lazy(() => import('@/pages/HomePage').then((m) => ({ default: m.HomePage })))
const LoginPage     = lazy(() => import('@/pages/LoginPage').then((m) => ({ default: m.LoginPage })))
const RegisterPage  = lazy(() => import('@/pages/RegisterPage').then((m) => ({ default: m.RegisterPage })))
const WorkspacePage = lazy(() => import('@/pages/WorkspacePage').then((m) => ({ default: m.WorkspacePage })))
const DashboardPage = lazy(() => import('@/pages/DashboardPage').then((m) => ({ default: m.DashboardPage })))

const PageLoader = () => (
  <div className="flex items-center justify-center h-screen" style={{ background: 'var(--bg-primary)' }}>
    <div className="flex flex-col items-center gap-3">
      <div className="w-8 h-8 rounded-lg animate-spin" style={{ border: '2px solid var(--border)', borderTopColor: 'var(--accent)' }} />
      <span className="text-xs font-mono" style={{ color: 'var(--text-muted)' }}>Loading...</span>
    </div>
  </div>
)

const App: React.FC = () => (
  <Suspense fallback={<PageLoader />}>
    <Routes>
      <Route element={<PublicLayout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
      </Route>
      <Route element={<AppLayout />}>
        <Route path="/workspace" element={<WorkspacePage />} />
        <Route path="/dashboard" element={<ProtectedRoute><DashboardPage /></ProtectedRoute>} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  </Suspense>
)

export default App
