import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type Theme = 'graphite' | 'aurora' | 'ivory' | 'matrix'

interface ThemeStore {
  theme: Theme
  setTheme: (theme: Theme) => void
}

export const THEMES: { id: Theme; name: string; description: string; preview: string[] }[] = [
  {
    id: 'graphite',
    name: 'Graphite',
    description: 'Minimal professional engineering',
    preview: ['#0d0d0f', '#5b8def', '#3ecf8e'],
  },
  {
    id: 'aurora',
    name: 'Aurora',
    description: 'Elegant futuristic mode',
    preview: ['#080c14', '#00d4ff', '#00e8a4'],
  },
  {
    id: 'ivory',
    name: 'Ivory',
    description: 'Clean light productivity',
    preview: ['#fafaf8', '#2563eb', '#16a34a'],
  },
  {
    id: 'matrix',
    name: 'Matrix',
    description: 'Terminal-inspired precision',
    preview: ['#000000', '#00ff41', '#00ccff'],
  },
]

export const useThemeStore = create<ThemeStore>()(
  persist(
    (set) => ({
      theme: 'graphite',
      setTheme: (theme) => {
        document.documentElement.setAttribute('data-theme', theme)
        set({ theme })
      },
    }),
    { name: 'reviora-theme' }
  )
)

// Initialize theme on load
const savedTheme = localStorage.getItem('reviora-theme')
if (savedTheme) {
  try {
    const { state } = JSON.parse(savedTheme)
    document.documentElement.setAttribute('data-theme', state?.theme || 'graphite')
  } catch {
    document.documentElement.setAttribute('data-theme', 'graphite')
  }
} else {
  document.documentElement.setAttribute('data-theme', 'graphite')
}
