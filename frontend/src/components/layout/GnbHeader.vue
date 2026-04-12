<script setup>
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const menus = [
  { label: '공지사항', path: '/boards/notice' },
  { label: '자유게시판', path: '/boards/free' },
  { label: '갤러리', path: '/boards/gallery' },
  { label: '문의게시판', path: '/boards/inquiry' },
]

function isActive(path) {
  return route.path.startsWith(path)
}

function handleLogout() {
  authStore.logout()
  router.push('/')
}
</script>

<template>
  <header class="gnb-header">
    <div class="gnb-inner">
      <RouterLink to="/" class="gnb-logo">포트폴리오</RouterLink>

      <nav class="gnb-nav">
        <RouterLink
          v-for="menu in menus"
          :key="menu.path"
          :to="menu.path"
          :class="['gnb-menu-item', { active: isActive(menu.path) }]"
        >
          {{ menu.label }}
        </RouterLink>
      </nav>

      <div class="gnb-auth">
        <template v-if="authStore.isLoggedIn">
          <span class="gnb-user-name">{{ authStore.user.name }}님 안녕하세요!</span>
          <button class="gnb-btn" @click="handleLogout">로그아웃</button>
        </template>
        <template v-else>
          <RouterLink to="/login" class="gnb-btn">로그인</RouterLink>
          <RouterLink to="/join" class="gnb-btn gnb-btn-outline">회원가입</RouterLink>
        </template>
      </div>
    </div>
  </header>
</template>

<style scoped>
.gnb-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
}
.gnb-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 32px;
}
.gnb-logo {
  font-size: 1.2rem;
  font-weight: 700;
  color: #333;
  text-decoration: none;
  white-space: nowrap;
}
.gnb-nav {
  display: flex;
  gap: 24px;
  flex: 1;
}
.gnb-menu-item {
  font-size: 0.95rem;
  color: #555;
  text-decoration: none;
  padding: 4px 0;
  border-bottom: 2px solid transparent;
  transition: color 0.2s, border-color 0.2s;
}
.gnb-menu-item:hover,
.gnb-menu-item.active {
  color: #1976d2;
  border-bottom-color: #1976d2;
}
.gnb-auth {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}
.gnb-user-name {
  font-size: 0.88rem;
  color: #555;
}
.gnb-btn {
  padding: 6px 14px;
  font-size: 0.88rem;
  border: 1px solid #1976d2;
  background: #1976d2;
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
  transition: background 0.2s;
}
.gnb-btn:hover {
  background: #1565c0;
}
.gnb-btn-outline {
  background: #fff;
  color: #1976d2;
}
.gnb-btn-outline:hover {
  background: #e3f2fd;
}
</style>
