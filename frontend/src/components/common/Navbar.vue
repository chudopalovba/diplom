<template>
  <nav class="navbar">
    <div class="navbar-brand">
      <span class="brand-icon">🚀</span>
      <span class="brand-text">DevOps Platform</span>
    </div>

    <ul class="nav-menu">
      <li class="nav-item">
        <router-link to="/dashboard" class="nav-link" active-class="active">
          <span class="nav-icon">📊</span>
          Дашборд
        </router-link>
      </li>
      <li class="nav-item">
        <router-link to="/projects/create" class="nav-link" active-class="active">
          <span class="nav-icon">➕</span>
          Новый проект
        </router-link>
      </li>
      <li class="nav-item">
        <router-link to="/profile" class="nav-link" active-class="active">
          <span class="nav-icon">👤</span>
          Профиль
        </router-link>
      </li>
    </ul>

    <div class="nav-footer">
      <div class="user-info" v-if="user">
        <div class="user-avatar">{{ userInitials }}</div>
        <div class="user-details">
          <span class="user-name">{{ user.username }}</span>
          <span class="user-email">{{ user.email }}</span>
        </div>
      </div>
      <button @click="handleLogout" class="logout-btn">
        <span>🚪</span> Выйти
      </button>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const user = computed(() => authStore.user)
const userInitials = computed(() => {
  if (!user.value) return '?'
  return user.value.username.substring(0, 2).toUpperCase()
})

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 250px;
  background: linear-gradient(180deg, #1a202c 0%, #2d3748 100%);
  color: white;
  display: flex;
  flex-direction: column;
  padding: 20px 0;
}

.navbar-brand {
  display: flex;
  align-items: center;
  padding: 0 20px 30px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.brand-icon {
  font-size: 28px;
  margin-right: 10px;
}

.brand-text {
  font-size: 18px;
  font-weight: 700;
}

.nav-menu {
  flex: 1;
  list-style: none;
  padding: 20px 0;
}

.nav-item {
  margin-bottom: 5px;
}

.nav-link {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  transition: all 0.2s ease;
  border-left: 3px solid transparent;
}

.nav-link:hover,
.nav-link.active {
  background-color: rgba(255, 255, 255, 0.1);
  color: white;
  border-left-color: #667eea;
}

.nav-icon {
  margin-right: 12px;
  font-size: 18px;
}

.nav-footer {
  padding: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-right: 10px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 500;
  font-size: 14px;
}

.user-email {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.logout-btn {
  width: 100%;
  padding: 10px;
  background-color: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 8px;
  color: white;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.logout-btn:hover {
  background-color: rgba(255, 255, 255, 0.2);
}
</style>