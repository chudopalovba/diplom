import { defineStore } from 'pinia'
import { projectService } from '../services/projectService'

export const useProjectStore = defineStore('project', {
  state: () => ({
    projects: [],
    currentProject: null,
    loading: false,
    error: null
  }),

  getters: {
    getProjectById: (state) => (id) => {
      return state.projects.find(p => p.id === id)
    },
    projectCount: (state) => state.projects.length,
    deployedProjects: (state) => state.projects.filter(p => p.status === 'deployed')
  },

  actions: {
    async fetchProjects() {
      this.loading = true
      this.error = null
      try {
        this.projects = await projectService.getProjects()
      } catch (error) {
        this.error = error.message
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchProject(id) {
      this.loading = true
      try {
        this.currentProject = await projectService.getProject(id)
        return this.currentProject
      } finally {
        this.loading = false
      }
    },

    async createProject(projectData) {
      this.loading = true
      try {
        const project = await projectService.createProject(projectData)
        this.projects.push(project)
        return project
      } finally {
        this.loading = false
      }
    },

    async deleteProject(id) {
      await projectService.deleteProject(id)
      this.projects = this.projects.filter(p => p.id !== id)
    }
  }
})