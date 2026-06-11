import axios, { AxiosError, AxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/store/authStore'

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export const api = axios.create({
  baseURL: `${BASE_URL}/api`,
  timeout: 30_000,
  headers: { 'Content-Type': 'application/json' },
})

// Request interceptor — attach token
api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Response interceptor — handle 401 refresh
api.interceptors.response.use(
  (res) => res,
  async (error: AxiosError) => {
    const original = error.config as AxiosRequestConfig & { _retry?: boolean }
    if (error.response?.status === 401 && !original._retry) {
      original._retry = true
      const refreshToken = useAuthStore.getState().refreshToken
      if (refreshToken) {
        try {
          const { data } = await axios.post(`${BASE_URL}/api/auth/refresh`, { refreshToken })
          useAuthStore.getState().updateToken(data.accessToken)
          return api(original)
        } catch {
          useAuthStore.getState().clearAuth()
          window.location.href = '/login'
        }
      }
    }
    return Promise.reject(error)
  }
)

// ─── Auth ───────────────────────────────────────────
export const authService = {
  login: (email: string, password: string) =>
    api.post('/auth/login', { email, password }).then((r) => r.data),

  register: (name: string, email: string, password: string) =>
    api.post('/auth/register', { name, email, password }).then((r) => r.data),

  googleLogin: (credential: string) =>
    api.post('/auth/google', { credential }).then((r) => r.data),

  refresh: (refreshToken: string) =>
    api.post('/auth/refresh', { refreshToken }).then((r) => r.data),

  me: () => api.get('/auth/me').then((r) => r.data),
}

// ─── Submissions ─────────────────────────────────────
export const submissionService = {
  submit: (payload: {
    code: string
    language: string
    stdin: string
  }) => api.post('/submissions', payload).then((r) => r.data),

  submitGuest: (payload: { code: string; language: string; stdin: string }) =>
    api.post('/submissions/guest', payload).then((r) => r.data),

  getById: (id: string) => api.get(`/submissions/${id}`).then((r) => r.data),

  getMySubmissions: (page = 0, size = 20) =>
    api.get('/submissions/me', { params: { page, size } }).then((r) => r.data),

  analyze: (id: string) =>
    api.post(`/submissions/${id}/analyze`).then((r) => r.data),

  getGrowthData: (id: string) =>
    api.get(`/submissions/${id}/growth`).then((r) => r.data),
}

// ─── AI ──────────────────────────────────────────────
export const aiService = {
  review: (submissionId: string) =>
    api.post(`/ai/review/${submissionId}`).then((r) => r.data),

  generateTests: (submissionId: string) =>
    api.post(`/ai/tests/${submissionId}`).then((r) => r.data),

  chat: (submissionId: string, message: string) =>
    api.post(`/ai/chat`, { submissionId, message }).then((r) => r.data),
}

// ─── Analytics ───────────────────────────────────────
export const analyticsService = {
  getDashboard: () => api.get('/analytics/dashboard').then((r) => r.data),
  getStreak: () => api.get('/analytics/streak').then((r) => r.data),
  getTopics: () => api.get('/analytics/topics').then((r) => r.data),
}

export type ApiError = {
  message: string
  status: number
  timestamp: string
}

export const getErrorMessage = (error: unknown): string => {
  if (axios.isAxiosError(error)) {
    return error.response?.data?.message || error.message || 'An error occurred'
  }
  return 'An unexpected error occurred'
}
