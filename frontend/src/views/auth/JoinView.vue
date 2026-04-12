<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToastStore } from '@/stores/toast'
import authApi from '@/api/auth'

const router = useRouter()
const toastStore = useToastStore()

const username = ref('')
const password = ref('')
const name = ref('')
const errorMsg = ref('')
const isLoading = ref(false)

async function handleJoin() {
  errorMsg.value = ''
  isLoading.value = true
  try {
    await authApi.join({ username: username.value, password: password.value, name: name.value })
    toastStore.show('success', '회원가입이 완료되었습니다. 로그인해주세요.')
    router.push('/login')
  } catch (error) {
    errorMsg.value = error.response?.data?.message || '회원가입에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="join-page">
    <div class="join-box">
      <h1 class="join-title">회원가입</h1>

      <div class="field">
        <label>아이디 <span class="hint">(영문+숫자 5~20자)</span></label>
        <input v-model="username" type="text" placeholder="아이디를 입력하세요" />
      </div>
      <div class="field">
        <label>비밀번호 <span class="hint">(영문+숫자 8자 이상)</span></label>
        <input v-model="password" type="password" placeholder="비밀번호를 입력하세요" />
      </div>
      <div class="field">
        <label>이름 <span class="hint">(1~20자)</span></label>
        <input v-model="name" type="text" placeholder="이름을 입력하세요" @keyup.enter="handleJoin" />
      </div>

      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

      <button class="btn-join" :disabled="isLoading" @click="handleJoin">
        {{ isLoading ? '가입 중...' : '회원가입' }}
      </button>

      <p class="login-link">
        이미 계정이 있으신가요?
        <RouterLink to="/login">로그인</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.join-page {
  display: flex;
  justify-content: center;
  padding-top: 60px;
}
.join-box {
  width: 400px;
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 40px 36px;
}
.join-title {
  font-size: 1.4rem;
  font-weight: 700;
  margin-bottom: 28px;
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
.hint { font-size: 0.78rem; color: #999; }
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
.btn-join {
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
.btn-join:hover:not(:disabled) { background: #1565c0; }
.btn-join:disabled { opacity: 0.6; cursor: not-allowed; }
.login-link {
  text-align: center;
  font-size: 0.85rem;
  color: #666;
  margin-top: 18px;
}
.login-link a {
  color: #1976d2;
  font-weight: 600;
}
</style>
