export const BACKEND_TECHNOLOGIES = [
  { value: 'java', label: 'Java (Spring Boot)', icon: '☕' },
  { value: 'csharp', label: 'C# (.NET)', icon: '🔷' },
  { value: 'python', label: 'Python (Django)', icon: '🐍' }
]

export const FRONTEND_TECHNOLOGIES = [
  { value: 'react', label: 'React', icon: '⚛️' },
  { value: 'vue', label: 'Vue.js', icon: '💚' },
  { value: 'angular', label: 'Angular', icon: '🔺' }
]

export const DATABASE_OPTIONS = [
  { value: 'postgres', label: 'PostgreSQL', icon: '🐘' }
]

export const PIPELINE_STATUSES = {
  pending: { label: 'Ожидание', class: 'badge-pending' },
  running: { label: 'Выполняется', class: 'badge-warning' },
  success: { label: 'Успешно', class: 'badge-success' },
  failed: { label: 'Ошибка', class: 'badge-danger' },
  canceled: { label: 'Отменён', class: 'badge-info' }
}

export const PROJECT_STATUSES = {
  created: { label: 'Создан', class: 'badge-info' },
  developing: { label: 'В разработке', class: 'badge-warning' },
  deployed: { label: 'Развёрнут', class: 'badge-success' },
  failed: { label: 'Ошибка', class: 'badge-danger' }
}