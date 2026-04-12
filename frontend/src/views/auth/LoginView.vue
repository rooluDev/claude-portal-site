<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import authApi from '@/api/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

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
    const ret = route.query.ret
    router.push(ret && ret.startsWith('/') ? ret : '/')
  } catch (error) {
    errorMsg.value = error.response?.data?.message || '로그인에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="login-box">
      <h1 class="login-title">로그인</h1>

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
        <RouterLink to="/join">회원가입</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  padding-top: 60px;
}
.login-box {
  width: 400px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 40px 36px;
}
.login-title {
  font-size: 1.4rem;
  font-weight: 700;
  margin-bottom: 28px;
  color: #333;
}
.field {
  margin-bottom: 18px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.field label {
  font-size: 0.88rem;
  color: #555;
}
.field input {
  padding: 11px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.2s;
}
.field input:focus { border-color: #1976d2; }
.error-msg {
  color: #e53935;
  font-size: 0.85rem;
  margin-bottom: 10px;
}
.btn-login {
  width: 100%;
  padding: 12px;
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-login:hover:not(:disabled) { background: #1565c0; }
.btn-login:disabled { opacity: 0.6; cursor: not-allowed; }
.join-link {
  text-align: center;
  font-size: 0.85rem;
  color: #666;
  margin-top: 18px;
}
.join-link a {
  color: #1976d2;
  font-weight: 600;
}
</style>
