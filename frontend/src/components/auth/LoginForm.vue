<template>
  <form @submit.prevent="handleSubmit" class="auth-form">
    <div class="form-group">
      <label class="form-label">Email</label>
      <input
        v-model="form.email"
        type="email"
        class="form-input"
        placeholder="your@email.com"
        required
      />
    </div>

    <div class="form-group">
      <label class="form-label">Пароль</label>
      <input
        v-model="form.password"
        type="password"
        class="form-input"
        placeholder="••••••••"
        required
      />
    </div>

    <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
      {{ loading ? 'Вход...' : 'Войти' }}
    </button>

    <p class="auth-link">
      Нет аккаунта? 
      <router-link to="/register">Зарегистрироваться</router-link>
    </p>
  </form>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useNotificationStore } from '../../stores/notification'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const loading = ref(false)
const form = ref({
  email: '',
  password: ''
})

const handleSubmit = async () => {
  loading.value = true
  try {
    await authStore.login(form.value)
    notificationStore.success('Добро пожаловать!')
    
    const redirect = route.query.redirect || '/dashboard'
    router.push(redirect)
  } catch (error) {
    notificationStore.error(error.response?.data?.message || 'Ошибка входа')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-form {
  width: 100%;
}

.btn-block {
  width: 100%;
  margin-top: 10px;
}

.auth-link {
  text-align: center;
  margin-top: 20px;
  color: #718096;
}

.auth-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.auth-link a:hover {
  text-decoration: underline;
}
</style>