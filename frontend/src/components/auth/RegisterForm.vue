<template>
  <form @submit.prevent="handleSubmit" class="auth-form">
    <div class="form-group">
      <label class="form-label">Имя пользователя</label>
      <input
        v-model="form.username"
        type="text"
        class="form-input"
        placeholder="username"
        required
      />
    </div>

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
        minlength="6"
        required
      />
    </div>

    <div class="form-group">
      <label class="form-label">Подтвердите пароль</label>
      <input
        v-model="form.confirmPassword"
        type="password"
        class="form-input"
        placeholder="••••••••"
        required
      />
      <span v-if="passwordMismatch" class="error-text">
        Пароли не совпадают
      </span>
    </div>

    <button 
      type="submit" 
      class="btn btn-primary btn-block" 
      :disabled="loading || passwordMismatch"
    >
      {{ loading ? 'Регистрация...' : 'Зарегистрироваться' }}
    </button>

    <p class="auth-link">
      Уже есть аккаунт? 
      <router-link to="/login">Войти</router-link>
    </p>
  </form>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { useNotificationStore } from '../../stores/notification'

const router = useRouter()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const loading = ref(false)
const form = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const passwordMismatch = computed(() => {
  return form.value.confirmPassword && form.value.password !== form.value.confirmPassword
})

const handleSubmit = async () => {
  if (passwordMismatch.value) return

  loading.value = true
  try {
    await authStore.register({
      username: form.value.username,
      email: form.value.email,
      password: form.value.password
    })
    notificationStore.success('Регистрация успешна!')
    router.push('/dashboard')
  } catch (error) {
    notificationStore.error(error.response?.data?.message || 'Ошибка регистрации')
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

.error-text {
  color: #e53e3e;
  font-size: 12px;
  margin-top: 4px;
}
</style>