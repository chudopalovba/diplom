<template>
  <div class="dashboard">
    <div class="page-header">
      <div>
        <h1 class="page-title">Дашборд</h1>
        <p class="page-subtitle">
          Добро пожаловать, {{ user?.username }}! 👋
        </p>
      </div>
      <router-link to="/projects/create" class="btn btn-primary">
        ➕ Новый проект
      </router-link>
    </div>

    <!-- Stats -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon">📁</div>
        <div class="stat-info">
          <span class="stat-value">{{ projectCount }}</span>
          <span class="stat-label">Всего проектов</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-info">
          <span class="stat-value">{{ deployedCount }}</span>
          <span class="stat-label">Развёрнуто</span>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🔄</div>
        <div class="stat-info">
          <span class="stat-value">{{ runningCount }}</span>
          <span class="stat-label">В процессе</span>
        </div>
      </div>
    </div>

    <!-- Projects -->
    <div class="projects-section">
      <h2 class="section-title">Мои проекты</h2>
      <ProjectList :projects="projects" :loading="loading" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useProjectStore } from '../stores/project'
import ProjectList from '../components/project/ProjectList.vue'

const authStore = useAuthStore()
const projectStore = useProjectStore()

const user = computed(() => authStore.user)
const projects = computed(() => projectStore.projects)
const loading = computed(() => projectStore.loading)
const projectCount = computed(() => projectStore.projectCount)
const deployedCount = computed(() => 
  projects.value.filter(p => p.status === 'deployed').length
)
const runningCount = computed(() => 
  projects.value.filter(p => p.status === 'developing').length
)

onMounted(() => {
  projectStore.fetchProjects()
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  font-size: 36px;
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  padding: 16px;
  border-radius: 12px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a202c;
}

.stat-label {
  font-size: 14px;
  color: #718096;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 20px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>