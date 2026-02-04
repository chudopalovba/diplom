<template>
  <div id="app">
    <Navbar v-if="isAuthenticated" />
    <main class="main-content" :class="{ 'no-nav': !isAuthenticated }">
      <Alert />
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from './stores/auth'
import Navbar from './components/common/Navbar.vue'
import Alert from './components/common/Alert.vue'

const authStore = useAuthStore()
const isAuthenticated = computed(() => authStore.isAuthenticated)
</script>

<style scoped>
.main-content {
  margin-left: 250px;
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.main-content.no-nav {
  margin-left: 0;
}
</style>