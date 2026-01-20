<template>
  <div class="pipeline-status">
    <div class="pipeline-header">
      <h3>Статус пайплайна</h3>
      <button @click="refresh" class="refresh-btn" :disabled="refreshing">
        🔄
      </button>
    </div>

    <div v-if="loading" class="pipeline-loading">
      <Loader text="Загрузка статуса..." />
    </div>

    <div v-else-if="!pipeline" class="pipeline-empty">
      <p>Пайплайн ещё не запускался</p>
    </div>

    <div v-else class="pipeline-content">
      <div class="pipeline-info">
        <span class="badge" :class="statusClass">{{ statusLabel }}</span>
        <span class="pipeline-id">#{{ pipeline.id }}</span>
      </div>

      <div class="pipeline-stages">
        <div
          v-for="stage in pipeline.stages"
          :key="stage.name"
          class="stage"
          :class="stage.status"
        >
          <div class="stage-icon">
            {{ getStageIcon(stage.status) }}
          </div>
          <span class="stage-name">{{ stage.name }}</span>
        </div>
      </div>

      <div v-if="pipeline.deployUrl" class="deploy-info">
        <span class="deploy-label">Развёрнуто:</span>
        <a :href="pipeline.deployUrl" target="_blank" class="deploy-link">
          {{ pipeline.deployUrl }}
        </a>
      </div>

      <div class="pipeline-time">
        <span>Запущен: {{ formatTime(pipeline.startedAt) }}</span>
        <span v-if="pipeline.finishedAt">
          Завершён: {{ formatTime(pipeline.finishedAt) }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { PIPELINE_STATUSES } from '../../utils/constants'
import Loader from '../common/Loader.vue'

const props = defineProps({
  pipeline: {
    type: Object,
    default: null
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['refresh'])

const refreshing = ref(false)
let refreshInterval = null

const statusClass = computed(() => {
  if (!props.pipeline) return ''
  return PIPELINE_STATUSES[props.pipeline.status]?.class || 'badge-info'
})

const statusLabel = computed(() => {
  if (!props.pipeline) return ''
  return PIPELINE_STATUSES[props.pipeline.status]?.label || props.pipeline.status
})

const getStageIcon = (status) => {
  const icons = {
    success: '✅',
    failed: '❌',
    running: '🔄',
    pending: '⏳',
    skipped: '⏭️'
  }
  return icons[status] || '⏳'
}

const formatTime = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('ru-RU')
}

const refresh = async () => {
  refreshing.value = true
  emit('refresh')
  setTimeout(() => {
    refreshing.value = false
  }, 1000)
}

// Auto-refresh при running статусе
onMounted(() => {
  if (props.pipeline?.status === 'running') {
    refreshInterval = setInterval(() => {
      emit('refresh')
    }, 10000)
  }
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<style scoped>
.pipeline-status {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.pipeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.pipeline-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
}

.refresh-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  padding: 5px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.refresh-btn:hover {
  background-color: #f7fafc;
}

.refresh-btn:disabled {
  animation: spin 1s linear infinite;
}

.pipeline-loading,
.pipeline-empty {
  text-align: center;
  padding: 30px;
  color: #718096;
}

.pipeline-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.pipeline-id {
  color: #718096;
  font-size: 14px;
}

.pipeline-stages {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.stage {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background-color: #f7fafc;
  border-radius: 20px;
  font-size: 13px;
}

.stage.success {
  background-color: #c6f6d5;
}

.stage.failed {
  background-color: #fed7d7;
}

.stage.running {
  background-color: #fefcbf;
}

.stage-icon {
  font-size: 14px;
}

.stage-name {
  font-weight: 500;
}

.deploy-info {
  background-color: #e6fffa;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 15px;
}

.deploy-label {
  font-size: 13px;
  color: #234e52;
  margin-right: 8px;
}

.deploy-link {
  color: #319795;
  text-decoration: none;
  font-weight: 500;
}

.deploy-link:hover {
  text-decoration: underline;
}

.pipeline-time {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #a0aec0;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>