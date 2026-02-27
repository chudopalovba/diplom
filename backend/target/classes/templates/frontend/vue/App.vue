<template>
  <div style="text-align: center; margin-top: 50px">
    <h1>{{PROJECT_NAME}}</h1>
    <p>{{ message }}</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const message = ref('Loading...')

onMounted(async () => {
  try {
    const res = await fetch('/api/hello')
    const data = await res.json()
    message.value = data.message
  } catch {
    message.value = '{{PROJECT_NAME}} — Frontend is running!'
  }
})
</script>