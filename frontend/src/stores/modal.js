import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useModalStore = defineStore('modal', () => {
  const isLoginModalOpen = ref(false)

  function openLoginModal() {
    isLoginModalOpen.value = true
  }

  function closeLoginModal() {
    isLoginModalOpen.value = false
  }

  return { isLoginModalOpen, openLoginModal, closeLoginModal }
})
