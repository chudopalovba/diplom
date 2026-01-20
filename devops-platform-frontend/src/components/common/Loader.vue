<template>
  <div class="loader-wrapper" :class="{ 'full-page': fullPage }">
    <div class="loader">
      <div class="loader-spinner"></div>
      <p v-if="text" class="loader-text">{{ text }}</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    default: ''
  },
  fullPage: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.loader-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
}

.loader-wrapper.full-page {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.9);
  z-index: 999;
}

.loader {
  text-align: center;
}

.loader-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e2e8f0;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loader-text {
  margin-top: 16px;
  color: #4a5568;
  font-size: 14px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>