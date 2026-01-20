<template>
  <div class="project-list">
    <div v-if="loading" class="loading-state">
      <Loader text="Загрузка проектов..." />
    </div>

    <div v-else-if="projects.length === 0" class="empty-state">
      <div class="empty-icon">📁</div>
      <h3>Нет проектов</h3>
      <p>Создайте свой первый проект, чтобы начать</p>
      <router-link to="/projects/create" class="btn btn-primary">
        Создать проект
      </router-link>
    </div>

    <div v-else class="projects-grid">
      <ProjectCard
        v-for="project in projects"
        :key="project.id"
        :project="project"
        @click="goToProject(project.id)"
      />
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import ProjectCard from './ProjectCard.vue'
import Loader from '../common/Loader.vue'

defineProps({
  projects: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const router = useRouter()

const goToProject = (id) => {
  router.push(`/projects/${id}`)
}
</script>

<style scoped>
.project-list {
  min-height: 300px;
}

.loading-state {
  display: flex;
  justify-content: center;
  padding: 60px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state h3 {
  font-size: 20px;
  color: #2d3748;
  margin-bottom: 10px;
}

.empty-state p {
  color: #718096;
  margin-bottom: 20px;
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}
</style>