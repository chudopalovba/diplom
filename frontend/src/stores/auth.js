import { defineStore } from 'pinia'
import { authService } from '../services/authService'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || null,
    loading: false
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    currentUser: (state) => state.user
  },

  actions: {
    async login(credentials) {
      this.loading = true
      try {
        const response = await authService.login(credentials)
        this.token = response.token
        this.user = response.user
        localStorage.setItem('token', response.token)
        return response
      } finally {
        this.loading = false
      }
    },

    async register(userData) {
      this.loading = true
      try {
        const response = await authService.register(userData)
        this.token = response.token
        this.user = response.user
        localStorage.setItem('token', response.token)
        return response
      } finally {
        this.loading = false
      }
    },

    async fetchCurrentUser() {
      if (!this.token) return null
      try {
        const user = await authService.getCurrentUser()
        this.user = user
        return user
      } catch (error) {
        this.logout()
        throw error
      }
    },

    async updateProfile(profileData) {
      const user = await authService.updateProfile(profileData)
      this.user = user
      return user
    },

    logout() {
      this.user = null
      this.token = null
      localStorage.removeItem('token')
    }
  }
})