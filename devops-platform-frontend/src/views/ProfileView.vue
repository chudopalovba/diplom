<template>
  <div class="profile-page">
    <div class="page-header">
      <h1 class="page-title">Профиль</h1>
      <p class="page-subtitle">Управление аккаунтом</p>
    </div>

    <div class="profile-grid">
      <!-- User Info -->
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">👤 Личная информация</h2>
        </div>
        <form @submit.prevent="updateProfile">
          <div class="form-group">
            <label class="form-label">Имя пользователя</label>
            <input
              v-model="profileForm.username"
              type="text"
              class="form-input"
              required
            />
          </div>
          <div class="form-group">
            <label class="form-label">Email</label>
            <input
              v-model="profileForm.email"
              type="email"
              class="form-input"
              required
            />
          </div>
          <button type="submit" class="btn btn-primary" :disabled="profileLoading">
            {{ profileLoading ? 'Сохранение...' : 'Сохранить' }}
          </button>
        </form>
      </div>

      <!-- Change Password -->
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">🔒 Изменить пароль</h2>
        </div>
        <form @submit.prevent="changePassword">
          <div class="form-group">
            <label class="form-label">Текущий пароль</label>
            <input
              v-model="passwordForm.currentPassword"
              type="password"
              class="form-input"
              required
            />
          </div>
          <div class="form-group">
            <label class="form-label">Новый пароль</label>
            <input
              v-model="passwordForm.newPassword"
              type="password"
              class="form-input"
              minlength="6"
              required
            />
          </div>
          <div class="form-group">
            <label class="form-label">Подтвердите новый пароль</label>
            <input
              v-model="passwordForm.confirmPassword"
              type="password"
              class="form-input"
              required
            />
          </div>
          <button type="submit" class="btn btn-primary" :disabled="passwordLoading">
            {{ passwordLoading ? 'Изменение...' : 'Изменить пароль' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '../stores/auth'
import { useNotificationStore } from '../stores/notification'
import { authService } from '../services/authService'

const authStore = useAuthStore()
const notificationStore = useNotificationStore()

const profileLoading = ref(false)
const passwordLoading = ref(false)

const profileForm = ref({
  username: '',
  email: ''
})

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

onMounted(() => {
  if (authStore.user) {
    profileForm.value = {
      username: authStore.user.username,
      email: authStore.user.email
    }
  }
})

const updateProfile = async () => {
  profileLoading.value = true
  try {
    await authStore.updateProfile(profileForm.value)
    notificationStore.success('Профиль обновлён!')
  } catch (error) {
    notificationStore.error(error.response?.data?.message || 'Ошибка обновления')
  } finally {
    profileLoading.value = false
  }
}

const changePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    notificationStore.error('Пароли не совпадают')
    return
  }

  passwordLoading.value = true
  try {
    await authService.changePassword({
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword
    })
    notificationStore.success('Пароль изменён!')
    passwordForm.value = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    notificationStore.error(error.response?.data?.message || 'Ошибка изменения пароля')
  } finally {
    passwordLoading.value = false
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 800px;
  margin: 0 auto;
}

.profile-grid {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
</style>