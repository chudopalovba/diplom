import api from './api'

export const projectService = {
  // Проекты
  async getProjects() {
    const response = await api.get('/projects')
    return response.data
  },

  async getProject(id) {
    const response = await api.get(`/projects/${id}`)
    return response.data
  },

  async createProject(projectData) {
    const response = await api.post('/projects', projectData)
    return response.data
  },

  async deleteProject(id) {
    const response = await api.delete(`/projects/${id}`)
    return response.data
  },

  // Pipeline операции
  async triggerBuild(projectId) {
    const response = await api.post(`/projects/${projectId}/build`)
    return response.data
  },

  async triggerDeploy(projectId) {
    const response = await api.post(`/projects/${projectId}/deploy`)
    return response.data
  },

  async runSonarQube(projectId) {
    const response = await api.post(`/projects/${projectId}/sonar`)
    return response.data
  },

  async getPipelineStatus(projectId) {
    const response = await api.get(`/projects/${projectId}/pipeline`)
    return response.data
  },

  async getPipelineHistory(projectId) {
    const response = await api.get(`/projects/${projectId}/pipeline/history`)
    return response.data
  },

  // GitLab интеграция
  async getGitlabInfo(projectId) {
    const response = await api.get(`/projects/${projectId}/gitlab`)
    return response.data
  },

  // Статистика
  async getProjectStats(projectId) {
    const response = await api.get(`/projects/${projectId}/stats`)
    return response.data
  }
}