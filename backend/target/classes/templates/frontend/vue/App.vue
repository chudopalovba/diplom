<template>
  <div class="app">
    <h1>{{PROJECT_NAME}}</h1>
    <p>{{ message }}</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const message = ref('Loading...')

onMounted(async () => {
  try {
    const { data } = await axios.get('/api/hello')
    message.value = data.message
  } catch {
    message.value = '{{PROJECT_NAME}} — Frontend is running!'
  }
})
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI',
               Roboto, Oxygen, Ubuntu, sans-serif;
  background: #f5f5f5; color: #333;
}
.app { text-align: center; margin-top: 50px; }
h1   { color: #42b883; }
</style>