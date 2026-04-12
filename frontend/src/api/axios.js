import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import { useModalStore } from '@/stores/modal'
import router from '@/router'

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
})

instance.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

instance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      const modalStore = useModalStore()
      authStore.logout()

      const currentRoute = router.currentRoute.value
      const protectedMeta = currentRoute.meta?.requiresAuth || currentRoute.meta?.requiresAdmin

      if (protectedMeta) {
        router.push(`/login?ret=${encodeURIComponent(currentRoute.fullPath)}`)
      } else {
        modalStore.openLoginModal()
      }
    }
    return Promise.reject(error)
  },
)

export default instance
