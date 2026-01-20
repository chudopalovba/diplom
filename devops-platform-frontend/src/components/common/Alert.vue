<template>
  <Transition name="slide">
    <div v-if="show" class="alert" :class="`alert-${type}`">
      <span class="alert-icon">{{ icon }}</span>
      <span class="alert-message">{{ message }}</span>
      <button @click="hide" class="alert-close">×</button>
    </div>
  </Transition>
</template>

<script setup>
import { computed } from 'vue'
import { useNotificationStore } from '../../stores/notification'

const notificationStore = useNotificationStore()

const show = computed(() => notificationStore.show)
const message = computed(() => notificationStore.message)
const type = computed(() => notificationStore.type)

const icon = computed(() => {
  const icons = {
    success: '✅',
    error: '❌',
    warning: '⚠️',
    info: 'ℹ️'
  }
  return icons[type.value] || icons.info
})

const hide = () => notificationStore.hide()
</script>

<style scoped>
.alert {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 16px 20px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 300px;
  max-width: 500px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
}

.alert-success {
  background-color: #c6f6d5;
  color: #22543d;
}

.alert-error {
  background-color: #fed7d7;
  color: #742a2a;
}

.alert-warning {
  background-color: #fefcbf;
  color: #744210;
}

.alert-info {
  background-color: #bee3f8;
  color: #2a4365;
}

.alert-icon {
  font-size: 20px;
}

.alert-message {
  flex: 1;
}

.alert-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  opacity: 0.7;
  line-height: 1;
}

.alert-close:hover {
  opacity: 1;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style>