<template>
  <div class="stack-selector">
    <!-- Backend -->
    <div class="stack-section">
      <h3 class="stack-title">Backend технология</h3>
      <div class="stack-options">
        <div
          v-for="tech in backendTechnologies"
          :key="tech.value"
          class="stack-option"
          :class="{ active: modelValue.backend === tech.value }"
          @click="selectBackend(tech.value)"
        >
          <span class="option-icon">{{ tech.icon }}</span>
          <span class="option-label">{{ tech.label }}</span>
        </div>
      </div>
    </div>

    <!-- Frontend -->
    <div class="stack-section">
      <h3 class="stack-title">Frontend технология</h3>
      <div class="stack-options">
        <div
          v-for="tech in frontendTechnologies"
          :key="tech.value"
          class="stack-option"
          :class="{ active: modelValue.frontend === tech.value }"
          @click="selectFrontend(tech.value)"
        >
          <span class="option-icon">{{ tech.icon }}</span>
          <span class="option-label">{{ tech.label }}</span>
        </div>
      </div>
    </div>

    <!-- Database -->
    <div class="stack-section">
      <h3 class="stack-title">База данных</h3>
      <div class="stack-options">
        <div
          v-for="db in databaseOptions"
          :key="db.value"
          class="stack-option"
          :class="{ active: modelValue.database === db.value }"
          @click="selectDatabase(db.value)"
        >
          <span class="option-icon">{{ db.icon }}</span>
          <span class="option-label">{{ db.label }}</span>
        </div>
      </div>
    </div>

    <!-- Docker -->
    <div class="stack-section">
      <h3 class="stack-title">Docker</h3>
      <label class="toggle-option">
        <input 
          type="checkbox" 
          :checked="modelValue.useDocker"
          @change="toggleDocker"
        />
        <span class="toggle-slider"></span>
        <span class="toggle-label">
          {{ modelValue.useDocker ? 'Docker контейнеризация включена' : 'Без Docker' }}
        </span>
      </label>
      <p class="option-hint">
        {{ modelValue.useDocker 
          ? 'Будет создан Dockerfile и настроен соответствующий CI/CD пайплайн' 
          : 'Приложение будет развёрнуто напрямую на сервере' 
        }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { 
  BACKEND_TECHNOLOGIES, 
  FRONTEND_TECHNOLOGIES, 
  DATABASE_OPTIONS 
} from '../../utils/constants'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue'])

const backendTechnologies = BACKEND_TECHNOLOGIES
const frontendTechnologies = FRONTEND_TECHNOLOGIES
const databaseOptions = DATABASE_OPTIONS

const selectBackend = (value) => {
  emit('update:modelValue', { ...props.modelValue, backend: value })
}

const selectFrontend = (value) => {
  emit('update:modelValue', { ...props.modelValue, frontend: value })
}

const selectDatabase = (value) => {
  emit('update:modelValue', { ...props.modelValue, database: value })
}

const toggleDocker = (event) => {
  emit('update:modelValue', { ...props.modelValue, useDocker: event.target.checked })
}
</script>

<style scoped>
.stack-selector {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.stack-section {
  padding-bottom: 25px;
  border-bottom: 1px solid #e2e8f0;
}

.stack-section:last-child {
  border-bottom: none;
}

.stack-title {
  font-size: 16px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 15px;
}

.stack-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stack-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: white;
}

.stack-option:hover {
  border-color: #a3bffa;
  background-color: #f7fafc;
}

.stack-option.active {
  border-color: #667eea;
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
}

.option-icon {
  font-size: 36px;
  margin-bottom: 10px;
}

.option-label {
  font-size: 14px;
  font-weight: 500;
  color: #4a5568;
  text-align: center;
}

/* Toggle switch */
.toggle-option {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 12px;
}

.toggle-option input {
  display: none;
}

.toggle-slider {
  width: 50px;
  height: 26px;
  background-color: #e2e8f0;
  border-radius: 13px;
  position: relative;
  transition: background-color 0.2s ease;
}

.toggle-slider::after {
  content: '';
  position: absolute;
  width: 22px;
  height: 22px;
  background-color: white;
  border-radius: 50%;
  top: 2px;
  left: 2px;
  transition: transform 0.2s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toggle-option input:checked + .toggle-slider {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.toggle-option input:checked + .toggle-slider::after {
  transform: translateX(24px);
}

.toggle-label {
  font-size: 14px;
  font-weight: 500;
  color: #4a5568;
}

.option-hint {
  margin-top: 10px;
  font-size: 13px;
  color: #718096;
}

@media (max-width: 768px) {
  .stack-options {
    grid-template-columns: 1fr;
  }
}
</style>