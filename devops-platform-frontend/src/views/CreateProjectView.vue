<template>
  <div class="create-project">
    <div class="page-header">
      <h1 class="page-title">Создание проекта</h1>
      <p class="page-subtitle">
        Настройте технологический стек для вашего нового проекта
      </p>
    </div>

    <form @submit.prevent="handleSubmit" class="project-form">
      <!-- Basic Info -->
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">📝 Основная информация</h2>
        </div>
        <div class="form-group">
          <label class="form-label">Название проекта *</label>
          <input
            v-model="form.name"
            type="text"
            class="form-input"
            placeholder="my-awesome-project"
            required
          />
          <small class="form-hint">
            Используйте только буквы, цифры и дефисы
          </small>
        </div>
        <div class="form-group">
          <label class="form-label">Описание</label>
          <textarea
            v-model="form.description"
            class="form-input"
            rows="3"
            placeholder="Краткое описание проекта..."
          ></textarea>
        </div>
      </div>

      <!-- Stack Selection -->
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">🛠️ Технологический стек</h2>
        </div>
        <StackSelector v-model="form.stack" />
      </div>

      <!-- Summary -->
      <div class="card summary-card">
        <div class="card-header">
          <h2 class="card-title">📋 Итого</h2>
        </div>
        <div class="summary-content">
          <div class="summary-item">
            <span class="summary-label">Backend:</span>
            <span class="summary-value">{{ getStackLabel('backend', form.stack.backend) }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">Frontend:</span>
            <span class="summary-value">{{ getStackLabel('frontend', form.stack.frontend) }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">Database:</span>
            <span class="summary-value">PostgreSQL</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">Docker:</span>
            <span class="summary-value">{{ form.stack.useDocker ? 'Да' : 'Нет' }}</span>
          </div>
        </div>
      </div>

      <!-- Actions -->
      <div class="form-actions">
        <router-link to="/dashboard" class="btn btn-secondary">
          Отмена
        </router-link>
        <button 
          type="submit" 
          class="btn btn-primary"
          :disabled="!isFormValid || loading"
        >
          {{ loading ? 'Создание...' : '🚀 Создать проект' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useProjectStore } from '../stores/project'
import { useNotificationStore } from '../stores/notification'
import { BACKEND_TECHNOLOGIES, FRONTEND_TECHNOLOGIES } from '../utils/constants'
import StackSelector from '../components/project/StackSelector.vue'

const router = useRouter()
const projectStore = useProjectStore()
const notificationStore = useNotificationStore()

const loading = ref(false)
const form = ref({
  name: '',
  description: '',
  stack: {
    backend: 'java',
    frontend: 'react',
    database: 'postgres',
    useDocker: true
  }
})

const isFormValid = computed(() => {
  return form.value.name.trim().length > 0 &&
    form.value.stack.backend &&
    form.value.stack.frontend
})

const getStackLabel = (type, value) => {
  const techs = type === 'backend' ? BACKEND_TECHNOLOGIES : FRONTEND_TECHNOLOGIES
  return techs.find(t => t.value === value)?.label || value
}

const handleSubmit = async () => {
  if (!isFormValid.value) return

  loading.value = true
  try {
    const project = await projectStore.createProject(form.value)
    notificationStore.success('Проект успешно создан!')
    router.push(`/projects/${project.id}`)
  } catch (error) {
    notificationStore.error(error.response?.data?.message || 'Ошибка создания проекта')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.create-project {
  max-width: 800px;
  margin: 0 auto;
}

.project-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-hint {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: #718096;
}

.summary-card {
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  border: 2px solid #667eea;
}

.summary-content {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  padding: 12px;
  background: white;
  border-radius: 8px;
}

.summary-label {
  color: #718096;
  font-size: 14px;
}

.summary-value {
  font-weight: 600;
  color: #2d3748;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding-top: 20px;
}
</style>