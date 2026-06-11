import React from 'react'
import { Outlet } from 'react-router-dom'
import { Navbar } from '@/components/shared/Navbar'

export const AppLayout: React.FC = () => {
  return (
    <div className="flex flex-col h-screen overflow-hidden" style={{ background: 'var(--bg-primary)' }}>
      <Navbar />
      <main className="flex-1 overflow-hidden">
        <Outlet />
      </main>
    </div>
  )
}

export const PublicLayout: React.FC = () => {
  return (
    <div className="min-h-screen" style={{ background: 'var(--bg-primary)' }}>
      <Navbar />
      <Outlet />
    </div>
  )
}
