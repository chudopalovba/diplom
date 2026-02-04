<template>
  <div class="project-card" @click="$emit('click')">
    <div class="card-header">
      <div class="project-icon">
        {{ getTechIcon(project.stack.backend) }}
      </div>
      <span class="badge" :class="statusClass">
        {{ statusLabel }}
      </span>
    </div>
    
    <h3 class="project-name">{{ project.name }}</h3>
    <p class="project-description">{{ project.description || 'Нет описания' }}</p>
    
    <div class="project-stack">
      <span class="stack-badge" :title="'Backend: ' + project.stack.backend">
        {{ getTechIcon(project.stack.backend) }}
      </span>
      <span class="stack-badge" :title="'Frontend: ' + project.stack.frontend">
        {{ getTechIcon(project.stack.frontend) }}
      </span>
      <span class="stack-badge" :title="'Database: ' + project.stack.database">
        🐘
      </span>
      <span v-if="project.stack.useDocker" class="stack-badge" title="Docker">
        🐳
      </span>
    </div>
    
    <div class="project-footer">
      <span class="project-date">
        {{ formatDate(project.createdAt) }}
      </span>
      <a 
        v-if="project.gitlabUrl" 
        :href="project.gitlabUrl" 
        target="_blank" 
        class="gitlab-link"
        @click.stop
      >
        GitLab →
      </a>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { PROJECT_STATUSES } from '../../utils/constants'

const props = defineProps({
  project: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])

const statusClass = computed(() => {
  return PROJECT_STATUSES[props.project.status]?.class || 'badge-info'
})

const statusLabel = computed(() => {
  return PROJECT_STATUSES[props.project.status]?.label || props.project.status
})

const getTechIcon = (tech) => {
  const icons = {
    java: '☕',
    csharp: '🔷',
    python: '🐍',
    react: '⚛️',
    vue: '💚',
    angular: '🔺',
    postgres: '🐘'
  }
  return icons[tech] || '📦'
}

const formatDate = (dateString) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('ru-RU', {
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  })
}
</script>

<style scoped>
.project-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.2s ease;
  border: 2px solid transparent;
}

.project-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  border-color: #667eea;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.project-icon {
  font-size: 32px;
}

.project-name {
  font-size: 18px;
  font-weight: 600;
  color: #1a202c;
  margin-bottom: 8px;
}

.project-description {
  font-size: 14px;
  color: #718096;
  margin-bottom: 15px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.project-stack {
  display: flex;
  gap: 8px;
  margin-bottom: 15px;
}

.stack-badge {
  font-size: 20px;
  background-color: #f7fafc;
  padding: 6px 10px;
  border-radius: 6px;
}

.project-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #e2e8f0;
}

.project-date {
  font-size: 12px;
  color: #a0aec0;
}

.gitlab-link {
  font-size: 13px;
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.gitlab-link:hover {
  text-decoration: underline;
}
</style>