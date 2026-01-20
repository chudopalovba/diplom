import api, { USE_MOCK } from './api'

export const authService = {
  async login(credentials) {
    if (USE_MOCK) {
      return await api.login(credentials)
    }
    const response = await api.post('/auth/login', credentials)
    return response.data
  },

  async register(userData) {
    if (USE_MOCK) {
      return await api.register(userData)
    }
    const response = await api.post('/auth/register', userData)
    return response.data
  },

  async getCurrentUser() {
    if (USE_MOCK) {
      return await api.getCurrentUser()
    }
    const response = await api.get('/auth/me')
    return response.data
  },

  async updateProfile(profileData) {
    if (USE_MOCK) {
      return await api.updateProfile(profileData)
    }
    const response = await api.put('/auth/profile', profileData)
    return response.data
  },

  async changePassword(passwordData) {
    if (USE_MOCK) {
      return { success: true }
    }
    const response = await api.put('/auth/password', passwordData)
    return response.data
  }
}