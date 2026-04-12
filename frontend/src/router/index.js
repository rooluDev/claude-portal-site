import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useModalStore } from '@/stores/modal'

const routes = [
  // 메인
  { path: '/', component: () => import('@/views/HomeView.vue') },

  // 인증
  { path: '/login', component: () => import('@/views/auth/LoginView.vue'), meta: { guestOnly: true } },
  { path: '/join', component: () => import('@/views/auth/JoinView.vue'), meta: { guestOnly: true } },

  // 공지사항
  { path: '/boards/notice', component: () => import('@/views/notice/NoticeListView.vue') },
  { path: '/boards/notice/:id', component: () => import('@/views/notice/NoticeDetailView.vue') },
  { path: '/boards/notice/write', component: () => import('@/views/notice/NoticeWriteView.vue'), meta: { requiresAdmin: true } },
  { path: '/boards/notice/modify/:id', component: () => import('@/views/notice/NoticeModifyView.vue'), meta: { requiresAdmin: true } },

  // 자유게시판
  { path: '/boards/free', component: () => import('@/views/free/FreeListView.vue') },
  { path: '/boards/free/:id', component: () => import('@/views/free/FreeDetailView.vue') },
  { path: '/boards/free/write', component: () => import('@/views/free/FreeWriteView.vue'), meta: { requiresAuth: true } },
  { path: '/boards/free/modify/:id', component: () => import('@/views/free/FreeModifyView.vue'), meta: { requiresAuth: true } },

  // 갤러리
  { path: '/boards/gallery', component: () => import('@/views/gallery/GalleryListView.vue') },
  { path: '/boards/gallery/:id', component: () => import('@/views/gallery/GalleryDetailView.vue') },
  { path: '/boards/gallery/write', component: () => import('@/views/gallery/GalleryWriteView.vue'), meta: { requiresAuth: true } },
  { path: '/boards/gallery/modify/:id', component: () => import('@/views/gallery/GalleryModifyView.vue'), meta: { requiresAuth: true } },

  // 문의게시판
  { path: '/boards/inquiry', component: () => import('@/views/inquiry/InquiryListView.vue') },
  { path: '/boards/inquiry/:id', component: () => import('@/views/inquiry/InquiryDetailView.vue') },
  { path: '/boards/inquiry/write', component: () => import('@/views/inquiry/InquiryWriteView.vue'), meta: { requiresAuth: true } },
  { path: '/boards/inquiry/modify/:id', component: () => import('@/views/inquiry/InquiryModifyView.vue'), meta: { requiresAuth: true } },

  // 404
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const modalStore = useModalStore()

  if (to.meta.guestOnly && authStore.isLoggedIn) {
    return next('/')
  }

  if (to.meta.requiresAdmin) {
    if (!authStore.isLoggedIn || !authStore.isAdmin) {
      return next('/')
    }
  }

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    modalStore.openLoginModal()
    return next(false)
  }

  next()
})

export default router
