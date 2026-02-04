<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-card">
        <div class="auth-header">
          <h1>🚀</h1>
          <h2>Вход в систему</h2>
          <p>Войдите в свой аккаунт DevOps Platform</p>
        </div>
        <LoginForm />
      </div>
    </div>
  </div>
</template>

<script setup>
import LoginForm from '../components/auth/LoginForm.vue'
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.auth-container {
  width: 100%;
  max-width: 420px;
}

.auth-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.auth-header {
  text-align: center;
  margin-bottom: 30px;
}

.auth-header h1 {
  font-size: 48px;
  margin-bottom: 10px;
}

.auth-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1a202c;
  margin-bottom: 8px;
}

.auth-header p {
  color: #718096;
}
</style>