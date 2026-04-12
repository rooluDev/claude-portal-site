<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToastStore } from '@/stores/toast'
import inquiryApi from '@/api/inquiry'

const route = useRoute()
const router = useRouter()
const toastStore = useToastStore()

const title = ref('')
const content = ref('')
const isSecret = ref(false)
const isLoading = ref(false)
const errorMsg = ref('')

async function fetchDetail() {
  try {
    const res = await inquiryApi.getDetail(route.params.id)
    const d = res.data.data
    if (!d.isEditable) {
      toastStore.show('error', '수정할 수 없습니다.')
      router.push('/boards/inquiry')
      return
    }
    title.value = d.title
    content.value = d.content
    isSecret.value = d.isSecret
  } catch (error) {
    const status = error.response?.status
    const msg = error.response?.data?.message
    if (status === 403) {
      toastStore.show('error', msg || '수정할 수 없습니다.')
    } else if (status !== 401) {
      toastStore.show('error', msg || '불러오기 실패')
    }
    router.push('/boards/inquiry')
  }
}

async function handleSubmit() {
  errorMsg.value = ''
  if (!title.value.trim()) { errorMsg.value = '제목을 입력해주세요.'; return }
  if (!content.value.trim()) { errorMsg.value = '내용을 입력해주세요.'; return }

  isLoading.value = true
  try {
    await inquiryApi.update(route.params.id, {
      title: title.value.trim(),
      content: content.value.trim(),
      isSecret: isSecret.value,
    })
    toastStore.show('success', '게시글이 수정되었습니다.')
    router.push(`/boards/inquiry/${route.params.id}`)
  } catch (error) {
    errorMsg.value = error.response?.data?.message || '오류가 발생했습니다.'
  } finally {
    isLoading.value = false
  }
}

onMounted(fetchDetail)
</script>

<template>
  <div class="write-page">
    <h1 class="write-title">문의 수정</h1>

    <div class="form">
      <div class="field">
        <label>제목</label>
        <input v-model="title" type="text" maxlength="255" />
      </div>

      <div class="field">
        <label>내용</label>
        <textarea v-model="content" rows="12" />
      </div>

      <div class="field check-field">
        <label>
          <input type="checkbox" v-model="isSecret" />
          비밀글로 작성
        </label>
      </div>

      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

      <div class="form-actions">
        <RouterLink :to="`/boards/inquiry/${route.params.id}`" class="btn-cancel">취소</RouterLink>
        <button class="btn-submit" :disabled="isLoading" @click="handleSubmit">
          {{ isLoading ? '수정 중...' : '수정' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.write-page { max-width: 860px; margin: 0 auto; }
.write-title { font-size: 1.4rem; font-weight: 700; margin-bottom: 24px; }
.form { display: flex; flex-direction: column; gap: 18px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field label { font-size: 0.88rem; color: #555; font-weight: 600; }
.field input[type=text], .field textarea { padding: 10px 12px; border: 1px solid #ccc; border-radius: 4px; font-size: 0.95rem; outline: none; font-family: inherit; }
.field input[type=text]:focus, .field textarea:focus { border-color: #1976d2; }
.field textarea { resize: vertical; }
.check-field label { display: flex; align-items: center; gap: 6px; cursor: pointer; font-weight: 400; }
.error-msg { color: #e53935; font-size: 0.85rem; }
.form-actions { display: flex; gap: 10px; justify-content: flex-end; }
.btn-cancel { padding: 10px 22px; border: 1px solid #ccc; border-radius: 4px; font-size: 0.9rem; color: #555; background: #fff; }
.btn-cancel:hover { background: #f5f5f5; }
.btn-submit { padding: 10px 22px; background: #1976d2; color: #fff; border: none; border-radius: 4px; font-size: 0.9rem; cursor: pointer; }
.btn-submit:hover:not(:disabled) { background: #1565c0; }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
