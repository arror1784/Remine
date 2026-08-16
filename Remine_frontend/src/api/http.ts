import axios from 'axios'
import { useAuthStore } from '@/store/auth'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
})

http.interceptors.request.use((config) => {
  const token = useAuthStore.getState().getActiveToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// No refresh endpoint exists — a 401 means the demo session is gone, so drop it
// and bounce to Splash, which re-runs demo login from scratch.
http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().clearSessions()
      window.location.href = '/'
    }
    return Promise.reject(error)
  }
)
