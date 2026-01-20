import { defineStore } from 'pinia'

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    message: null,
    type: 'info', // 'success', 'error', 'warning', 'info'
    show: false
  }),

  actions: {
    showNotification(message, type = 'info') {
      this.message = message
      this.type = type
      this.show = true
      
      setTimeout(() => {
        this.hide()
      }, 5000)
    },

    success(message) {
      this.showNotification(message, 'success')
    },

    error(message) {
      this.showNotification(message, 'error')
    },

    warning(message) {
      this.showNotification(message, 'warning')
    },

    info(message) {
      this.showNotification(message, 'info')
    },

    hide() {
      this.show = false
      this.message = null
    }
  }
})