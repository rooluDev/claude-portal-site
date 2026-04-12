<script setup>
import GnbHeader from '@/components/layout/GnbHeader.vue'
import LoginModal from '@/components/common/LoginModal.vue'
import ToastMessage from '@/components/common/ToastMessage.vue'
</script>

<template>
  <GnbHeader />
  <main class="main-content">
    <RouterView />
  </main>
  <LoginModal />
  <ToastMessage />
</template>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}
body {
  font-family: 'Noto Sans KR', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  color: #333;
  background: #fafafa;
}
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
  min-height: calc(100vh - 60px);
}
a { text-decoration: none; color: inherit; }
button { font-family: inherit; }
input, textarea, select { font-family: inherit; }
</style>
