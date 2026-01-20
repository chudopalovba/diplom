import api, { USE_MOCK } from './api'

export const projectService = {
  async getProjects() {
    if (USE_MOCK) {
      return await api.getProjects()
    }
    const response = await api.get('/projects')
    return response.data
  },

  async getProject(id) {
    if (USE_MOCK) {
      return await api.getProject(id)
    }
    const response = await api.get(`/projects/${id}`)
    return response.data
  },

  async createProject(projectData) {
    if (USE_MOCK) {
      return await api.createProject(projectData)
    }
    const response = await api.post('/projects', projectData)
    return response.data
  },

  async deleteProject(id) {
    if (USE_MOCK) {
      return await api.deleteProject(id)
    }
    const response = await api.delete(`/projects/${id}`)
    return response.data
  },

  async triggerBuild(projectId) {
    if (USE_MOCK) {
      return await api.triggerBuild(projectId)
    }
    const response = await api.post(`/projects/${projectId}/build`)
    return response.data
  },

  async triggerDeploy(projectId) {
    if (USE_MOCK) {
      return await api.triggerDeploy(projectId)
    }
    const response = await api.post(`/projects/${projectId}/deploy`)
    return response.data
  },

  async runSonarQube(projectId) {
    if (USE_MOCK) {
      return await api.runSonarQube(projectId)
    }
    const response = await api.post(`/projects/${projectId}/sonar`)
    return response.data
  },

  async getPipelineStatus(projectId) {
    if (USE_MOCK) {
      return await api.getPipelineStatus(projectId)
    }
    const response = await api.get(`/projects/${projectId}/pipeline`)
    return response.data
  },

  async getPipelineHistory(projectId) {
    if (USE_MOCK) {
      return []
    }
    const response = await api.get(`/projects/${projectId}/pipeline/history`)
    return response.data
  },

  async getGitlabInfo(projectId) {
    if (USE_MOCK) {
      const project = await api.getProject(projectId)
      return { url: project.gitlabUrl, cloneUrl: project.gitCloneUrl }
    }
    const response = await api.get(`/projects/${projectId}/gitlab`)
    return response.data
  },

  async getProjectStats(projectId) {
    if (USE_MOCK) {
      return { builds: 10, deploys: 5, lastActivity: new Date().toISOString() }
    }
    const response = await api.get(`/projects/${projectId}/stats`)
    return response.data
  }
}