<template>
  <div class="project-detail">
    <Loader v-if="loading" text="Загрузка проекта..." full-page />
    
    <template v-else-if="project">
      <!-- Header -->
      <div class="page-header">
        <div class="header-info">
          <router-link to="/dashboard" class="back-link">← Назад</router-link>
          <h1 class="page-title">{{ project.name }}</h1>
          <span class="badge" :class="statusClass">{{ statusLabel }}</span>
        </div>
        <div class="header-actions">
          <button 
            @click="runBuild" 
            class="btn btn-secondary"
            :disabled="actionLoading"
          >
            🔨 Собрать
          </button>
          <button 
            @click="runSonar" 
            class="btn btn-secondary"
            :disabled="actionLoading"
          >
            🔍 SonarQube
          </button>
          <button 
            @click="runDeploy" 
            class="btn btn-primary"
            :disabled="actionLoading"
          >
            🚀 Развернуть
          </button>
        </div>
      </div>

      <div class="project-content">
        <div class="main-column">
          <!-- Project Info -->
          <div class="card">
            <div class="card-header">
              <h2 class="card-title">📋 Информация о проекте</h2>
            </div>
            <p v-if="project.description" class="project-description">
              {{ project.description }}
            </p>
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">Backend</span>
                <span class="info-value">{{ getTechLabel(project.stack.backend) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">Frontend</span>
                <span class="info-value">{{ getTechLabel(project.stack.frontend) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">Database</span>
                <span class="info-value">PostgreSQL</span>
              </div>
              <div class="info-item">
                <span class="info-label">Docker</span>
                <span class="info-value">{{ project.stack.useDocker ? 'Да' : 'Нет' }}</span>
              </div>
            </div>
          </div>

          <!-- GitLab Info -->
          <div class="card">
            <div class="card-header">
              <h2 class="card-title">🦊 GitLab</h2>
            </div>
            <div class="gitlab-info">
              <div class="gitlab-url">
                <span class="info-label">Репозиторий:</span>
                <a :href="project.gitlabUrl" target="_blank" class="link">
                  {{ project.gitlabUrl }}
                </a>
              </div>
              <div class="git-clone">
                <code>git clone {{ project.gitCloneUrl }}</code>
                <button @click="copyCloneUrl" class="copy-btn">📋</button>
              </div>
            </div>
          </div>
        </div>

        <div class="side-column">
          <!-- Pipeline Status -->
          <PipelineStatus 
            :pipeline="pipelineStatus"
            :loading="pipelineLoading"
            @refresh="fetchPipelineStatus"
          />

          <!-- Quick Actions -->
          <div class="card">
            <div class="card-header">
              <h2 class="card-title">⚡ Быстрые действия</h2>
            </div>
            <div class="quick-actions">
              <a :href="project.gitlabUrl" target="_blank" class="action-link">
                🦊 Открыть в GitLab
              </a>
              <a 
                v-if="project.deployUrl" 
                :href="project.deployUrl" 
                target="_blank" 
                class="action-link"
              >
                🌐 Открыть приложение
              </a>
              <button @click="deleteProject" class="action-link danger">
                🗑️ Удалить проект
              </button>
            </div>
          </div>
        </div>
      </div>
    </template>

    <div v-else class="not-found">
      <h2>Проект не найден</h2>
      <router-link to="/dashboard" class="btn btn-primary">
        Вернуться на дашборд
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProjectStore } from '../stores/project'
import { useNotificationStore } from '../stores/notification'
import { PROJECT_STATUSES, BACKEND_TECHNOLOGIES, FRONTEND_TECHNOLOGIES } from '../utils/constants'
import Loader from '../components/common/Loader.vue'
import PipelineStatus from '../components/project/PipelineStatus.vue'

const route = useRoute()
const router = useRouter()
const projectStore = useProjectStore()
const notificationStore = useNotificationStore()

const loading = ref(true)
const actionLoading = ref(false)
const pipelineLoading = ref(false)
const project = computed(() => projectStore.currentProject)
const pipelineStatus = computed(() => projectStore.pipelineStatus)

const statusClass = computed(() => 
  PROJECT_STATUSES[project.value?.status]?.class || 'badge-info'
)
const statusLabel = computed(() => 
  PROJECT_STATUSES[project.value?.status]?.label || ''
)

const getTechLabel = (value) => {
  const all = [...BACKEND_TECHNOLOGIES, ...FRONTEND_TECHNOLOGIES]
  return all.find(t => t.value === value)?.label || value
}

const fetchProject = async () => {
  try {
    await projectStore.fetchProject(route.params.id)
  } finally {
    loading.value = false
  }
}

const fetchPipelineStatus = async () => {
  pipelineLoading.value = true
  try {
    await projectStore.fetchPipelineStatus(route.params.id)
  } finally {
    pipelineLoading.value = false
  }
}

const runBuild = async () => {
  actionLoading.value = true
  try {
    await projectStore.triggerBuild(route.params.id)
    notificationStore.success('Сборка запущена!')
    fetchPipelineStatus()
  } catch (error) {
    notificationStore.error('Ошибка запуска сборки')
  } finally {
    actionLoading.value = false
  }
}

const runSonar = async () => {
  actionLoading.value = true
  try {
    await projectStore.runSonarQube(route.params.id)
    notificationStore.success('Анализ SonarQube запущен!')
    fetchPipelineStatus()
  } catch (error) {
    notificationStore.error('Ошибка запуска анализа')
  } finally {
    actionLoading.value = false
  }
}

const runDeploy = async () => {
  actionLoading.value = true
  try {
    await projectStore.triggerDeploy(route.params.id)
    notificationStore.success('Деплой запущен!')
    fetchPipelineStatus()
  } catch (error) {
    notificationStore.error('Ошибка запуска деплоя')
  } finally {
    actionLoading.value = false
  }
}

const copyCloneUrl = () => {
  navigator.clipboard.writeText(project.value.gitCloneUrl)
  notificationStore.success('URL скопирован!')
}

const deleteProject = async () => {
  if (!confirm('Вы уверены, что хотите удалить проект?')) return
  
  try {
    await projectStore.deleteProject(route.params.id)
    notificationStore.success('Проект удалён')
    router.push('/dashboard')
  } catch (error) {
    notificationStore.error('Ошибка удаления проекта')
  }
}

onMounted(() => {
  fetchProject()
  fetchPipelineStatus()
})
</script>

<style scoped>
.project-detail {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
}

.back-link {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  display: block;
  margin-bottom: 8px;
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.header-info .page-title {
  margin-bottom: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.project-content {
  display: grid;
  grid-template-columns: 1fr 380px;
  gap: 24px;
}

.main-column,
.side-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.project-description {
  color: #4a5568;
  line-height: 1.6;
  margin-bottom: 20px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #718096;
  text-transform: uppercase;
}

.info-value {
  font-size: 16px;
  font-weight: 500;
  color: #2d3748;
}

.gitlab-url {
  margin-bottom: 16px;
}

.link {
  color: #667eea;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

.git-clone {
  display: flex;
  align-items: center;
  background-color: #1a202c;
  padding: 12px 16px;
  border-radius: 8px;
}

.git-clone code {
  flex: 1;
  color: #68d391;
  font-family: monospace;
  font-size: 13px;
}

.copy-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-link {
  display: block;
  padding: 12px 16px;
  background-color: #f7fafc;
  border-radius: 8px;
  color: #4a5568;
  text-decoration: none;
  font-size: 14px;
  border: none;
  text-align: left;
  cursor: pointer;
  transition: background-color 0.2s;
}

.action-link:hover {
  background-color: #edf2f7;
}

.action-link.danger {
  color: #e53e3e;
}

.action-link.danger:hover {
  background-color: #fed7d7;
}

.not-found {
  text-align: center;
  padding: 60px;
}

@media (max-width: 968px) {
  .project-content {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    gap: 20px;
  }
  
  .header-actions {
    flex-wrap: wrap;
  }
}
</style>