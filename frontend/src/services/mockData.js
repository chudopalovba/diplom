// src/services/mockData.js

// Имитация задержки сети
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms))

// Мок пользователи
const users = [
  {
    id: 1,
    username: 'testuser',
    email: 'test@example.com',
    password: '123456'
  }
]

// Мок проекты
let projects = [
  {
    id: 1,
    name: 'my-first-project',
    description: 'Тестовый проект на Java + React',
    status: 'deployed',
    stack: {
      backend: 'java',
      frontend: 'react',
      database: 'postgres',
      useDocker: true
    },
    gitlabUrl: 'http://gitlab.local/testuser/my-first-project',
    gitCloneUrl: 'git@gitlab.local:testuser/my-first-project.git',
    deployUrl: 'http://my-first-project.apps.local',
    createdAt: '2024-01-15T10:30:00Z'
  },
  {
    id: 2,
    name: 'python-api',
    description: 'REST API на Django',
    status: 'developing',
    stack: {
      backend: 'python',
      frontend: 'vue',
      database: 'postgres',
      useDocker: false
    },
    gitlabUrl: 'http://gitlab.local/testuser/python-api',
    gitCloneUrl: 'git@gitlab.local:testuser/python-api.git',
    deployUrl: null,
    createdAt: '2024-01-20T14:00:00Z'
  }
]

// Мок пайплайн
const mockPipeline = {
  id: 42,
  status: 'success',
  stages: [
    { name: 'build', status: 'success' },
    { name: 'test', status: 'success' },
    { name: 'sonar', status: 'success' },
    { name: 'deploy', status: 'success' }
  ],
  deployUrl: 'http://my-first-project.apps.local',
  startedAt: '2024-01-20T15:30:00Z',
  finishedAt: '2024-01-20T15:35:00Z'
}

let nextProjectId = 3
let currentUser = null
let authToken = null

// Mock API
export const mockApi = {
  // Auth
  async login(credentials) {
    await delay(500)
    
    const user = users.find(u => 
      u.email === credentials.email && u.password === credentials.password
    )
    
    if (!user) {
      throw { response: { status: 401, data: { message: 'Неверный email или пароль' } } }
    }
    
    authToken = 'mock-jwt-token-' + Date.now()
    currentUser = { id: user.id, username: user.username, email: user.email }
    
    return {
      token: authToken,
      user: currentUser
    }
  },

  async register(userData) {
    await delay(500)
    
    if (users.find(u => u.email === userData.email)) {
      throw { response: { status: 400, data: { message: 'Email уже используется' } } }
    }
    
    const newUser = {
      id: users.length + 1,
      ...userData
    }
    users.push(newUser)
    
    authToken = 'mock-jwt-token-' + Date.now()
    currentUser = { id: newUser.id, username: newUser.username, email: newUser.email }
    
    return {
      token: authToken,
      user: currentUser
    }
  },

  async getCurrentUser() {
    await delay(200)
    
    if (!currentUser) {
      throw { response: { status: 401 } }
    }
    
    return currentUser
  },

  async updateProfile(profileData) {
    await delay(300)
    currentUser = { ...currentUser, ...profileData }
    return currentUser
  },

  // Projects
  async getProjects() {
    await delay(400)
    return [...projects]
  },

  async getProject(id) {
    await delay(300)
    const project = projects.find(p => p.id === parseInt(id))
    
    if (!project) {
      throw { response: { status: 404, data: { message: 'Проект не найден' } } }
    }
    
    return project
  },

  async createProject(projectData) {
    await delay(800)
    
    const newProject = {
      id: nextProjectId++,
      name: projectData.name,
      description: projectData.description,
      status: 'created',
      stack: projectData.stack,
      gitlabUrl: `http://gitlab.local/${currentUser?.username || 'user'}/${projectData.name}`,
      gitCloneUrl: `git@gitlab.local:${currentUser?.username || 'user'}/${projectData.name}.git`,
      deployUrl: null,
      createdAt: new Date().toISOString()
    }
    
    projects.push(newProject)
    return newProject
  },

  async deleteProject(id) {
    await delay(300)
    projects = projects.filter(p => p.id !== parseInt(id))
    return { success: true }
  },

  // Pipeline
  async getPipelineStatus(projectId) {
    await delay(300)
    return { ...mockPipeline }
  },

  async triggerBuild(projectId) {
    await delay(500)
    return { message: 'Build started', pipelineId: 43 }
  },

  async triggerDeploy(projectId) {
    await delay(500)
    
    const project = projects.find(p => p.id === parseInt(projectId))
    if (project) {
      project.status = 'deployed'
      project.deployUrl = `http://${project.name}.apps.local`
    }
    
    return { message: 'Deploy started', pipelineId: 44 }
  },

  async runSonarQube(projectId) {
    await delay(500)
    return { message: 'SonarQube analysis started' }
  }
}