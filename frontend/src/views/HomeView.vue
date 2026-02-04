<template>
  <div class="home-page">
    <div class="hero-section">
      <h1 class="hero-title">
        🚀 DevOps Platform
      </h1>
      <p class="hero-subtitle">
        Создавайте проекты с готовой CI/CD инфраструктурой
        за считанные минуты
      </p>
      <div class="hero-actions">
        <router-link to="/register" class="btn btn-primary btn-lg">
          Начать бесплатно
        </router-link>
        <router-link to="/login" class="btn btn-secondary btn-lg">
          Войти
        </router-link>
      </div>
    </div>

    <div class="features-section">
      <h2 class="section-title">Возможности платформы</h2>
      <div class="features-grid">
        <div class="feature-card">
          <div class="feature-icon">⚡</div>
          <h3>Быстрый старт</h3>
          <p>Выберите технологический стек и получите готовый репозиторий с настроенным CI/CD</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">🔧</div>
          <h3>Гибкий стек</h3>
          <p>Java, C#, Python для бэкенда. React, Vue, Angular для фронтенда</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">🐳</div>
          <h3>Docker-ready</h3>
          <p>Опциональная контейнеризация с автоматической генерацией Dockerfile</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">🔍</div>
          <h3>Качество кода</h3>
          <p>Интеграция с SonarQube для анализа качества кода</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">🚀</div>
          <h3>Автодеплой</h3>
          <p>Автоматическое развёртывание на сервере после успешной сборки</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">📦</div>
          <h3>Nexus репозиторий</h3>
          <p>Хранение артефактов в приватном Nexus репозитории</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
}

.hero-section {
  text-align: center;
  padding: 100px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 20px;
}

.hero-subtitle {
  font-size: 20px;
  opacity: 0.9;
  max-width: 600px;
  margin: 0 auto 40px;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.btn-lg {
  padding: 16px 32px;
  font-size: 16px;
}

.features-section {
  padding: 80px 40px;
  max-width: 1200px;
  margin: 0 auto;
}

.section-title {
  text-align: center;
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 50px;
  color: #1a202c;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
}

.feature-card {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  text-align: center;
  transition: transform 0.2s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 20px;
}

.feature-card h3 {
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 12px;
}

.feature-card p {
  color: #718096;
  line-height: 1.6;
  font-size: 14px;
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .hero-actions {
    flex-direction: column;
    align-items: center;
  }
}
</style>