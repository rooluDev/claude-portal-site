<script setup>
import { ref } from 'vue'
import { useModalStore } from '@/stores/modal'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import { useRouter } from 'vue-router'
import authApi from '@/api/auth'

const modalStore = useModalStore()
const authStore = useAuthStore()
const toastStore = useToastStore()
const router = useRouter()

const username = ref('')
const password = ref('')
const errorMsg = ref('')
const isLoading = ref(false)

async function handleLogin() {
  errorMsg.value = ''
  isLoading.value = true
  try {
    const res = await authApi.login({ username: username.value, password: password.value })
    const { token, user } = res.data.data
    authStore.login(token, user)
    modalStore.closeLoginModal()
    username.value = ''
    password.value = ''
  } catch (error) {
    errorMsg.value = error.response?.data?.message || '로그인에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

function handleBackdropClick(e) {
  if (e.target === e.currentTarget) {
    modalStore.closeLoginModal()
  }
}

function handleKeydown(e) {
  if (e.key === 'Escape') modalStore.closeLoginModal()
}

function goToJoin() {
  modalStore.closeLoginModal()
  router.push('/join')
}
</script>

<template>
  <div
    v-if="modalStore.isLoginModalOpen"
    class="modal-backdrop"
    @click="handleBackdropClick"
    @keydown="handleKeydown"
    tabindex="-1"
  >
    <div class="modal-box" role="dialog" aria-modal="true">
      <h2 class="modal-title">로그인</h2>

      <div class="field">
        <label>아이디</label>
        <input v-model="username" type="text" placeholder="아이디를 입력하세요" @keyup.enter="handleLogin" />
      </div>
      <div class="field">
        <label>비밀번호</label>
        <input v-model="password" type="password" placeholder="비밀번호를 입력하세요" @keyup.enter="handleLogin" />
      </div>

      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

      <button class="btn-login" :disabled="isLoading" @click="handleLogin">
        {{ isLoading ? '로그인 중...' : '로그인' }}
      </button>

      <p class="join-link">
        계정이 없으신가요?
        <a href="#" @click.prevent="goToJoin">회원가입</a>
      </p>
    </div>
  </div>
</template>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal-box {
  background: #fff;
  border-radius: 8px;
  padding: 32px 28px;
  width: 360px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}
.modal-title {
  font-size: 1.3rem;
  font-weight: 700;
  margin-bottom: 24px;
  color: #333;
}
.field {
  margin-bottom: 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.field label {
  font-size: 0.88rem;
  color: #555;
}
.field input {
  padding: 10px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.2s;
}
.field input:focus {
  border-color: #1976d2;
}
.error-msg {
  color: #e53935;
  font-size: 0.85rem;
  margin-bottom: 8px;
}
.btn-login {
  width: 100%;
  padding: 11px;
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-login:hover:not(:disabled) {
  background: #1565c0;
}
.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.join-link {
  text-align: center;
  font-size: 0.85rem;
  color: #666;
  margin-top: 16px;
}
.join-link a {
  color: #1976d2;
  text-decoration: none;
}
.join-link a:hover {
  text-decoration: underline;
}
</style>
