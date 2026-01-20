// src/services/api.js
import axios from 'axios'
import { mockApi } from './mockData'

// Флаг для переключения между mock и реальным API
const USE_MOCK = true  // Поставь false когда будет готов бэкенд

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// Экспортируем либо mock, либо реальный API
export default USE_MOCK ? mockApi : api
export { USE_MOCK }