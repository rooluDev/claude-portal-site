<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToastStore } from '@/stores/toast'
import noticeApi from '@/api/notice'

const route = useRoute()
const router = useRouter()
const toastStore = useToastStore()

const category = ref('NOTICE')
const isPinned = ref(false)
const title = ref('')
const content = ref('')
const isLoading = ref(false)
const errorMsg = ref('')

async function fetchDetail() {
  try {
    const res = await noticeApi.getDetail(route.params.id)
    const d = res.data.data
    category.value = d.category
    isPinned.value = d.isPinned
    title.value = d.title
    content.value = d.content
  } catch (error) {
    const msg = error.response?.data?.message
    if (error.response?.status !== 401) toastStore.show('error', msg || '불러오기 실패')
    router.push('/boards/notice')
  }
}

async function handleSubmit() {
  errorMsg.value = ''
  if (!title.value.trim()) { errorMsg.value = '제목을 입력해주세요.'; return }
  if (!content.value.trim()) { errorMsg.value = '내용을 입력해주세요.'; return }

  isLoading.value = true
  try {
    await noticeApi.update(route.params.id, {
      category: category.value,
      isPinned: isPinned.value,
      title: title.value.trim(),
      content: content.value.trim(),
    })
    toastStore.show('success', '게시글이 수정되었습니다.')
    router.push(`/boards/notice/${route.params.id}`)
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
    <h1 class="write-title">공지사항 수정</h1>

    <div class="form">
      <div class="field">
        <label>분류</label>
        <select v-model="category">
          <option value="NOTICE">공지</option>
          <option value="EVENT">이벤트</option>
        </select>
      </div>

      <div class="field check-field">
        <label>
          <input type="checkbox" v-model="isPinned" />
          고정글 설정
        </label>
      </div>

      <div class="field">
        <label>제목</label>
        <input v-model="title" type="text" maxlength="255" />
      </div>

      <div class="field">
        <label>내용</label>
        <textarea v-model="content" rows="14" />
      </div>

      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

      <div class="form-actions">
        <RouterLink :to="`/boards/notice/${route.params.id}`" class="btn-cancel">취소</RouterLink>
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
.field input[type=text], .field select, .field textarea {
  padding: 10px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.95rem;
  outline: none;
  font-family: inherit;
}
.field input[type=text]:focus, .field select:focus, .field textarea:focus { border-color: #1976d2; }
.field textarea { resize: vertical; }
.error-msg { color: #e53935; font-size: 0.85rem; }
.form-actions { display: flex; gap: 10px; justify-content: flex-end; }
.btn-cancel {
  padding: 10px 22px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.9rem;
  color: #555;
  background: #fff;
}
.btn-cancel:hover { background: #f5f5f5; }
.btn-submit {
  padding: 10px 22px;
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 0.9rem;
  cursor: pointer;
}
.btn-submit:hover:not(:disabled) { background: #1565c0; }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
