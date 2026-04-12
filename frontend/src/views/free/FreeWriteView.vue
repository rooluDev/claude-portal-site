<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useToastStore } from '@/stores/toast'
import FileAttachment from '@/components/file/FileAttachment.vue'
import freeApi from '@/api/free'

const router = useRouter()
const toastStore = useToastStore()

const category = ref('HUMOR')
const title = ref('')
const content = ref('')
const newFiles = ref([])
const isLoading = ref(false)
const errorMsg = ref('')

function handleNewFiles(files) { newFiles.value = files }

async function handleSubmit() {
  errorMsg.value = ''
  if (!title.value.trim()) { errorMsg.value = '제목을 입력해주세요.'; return }
  if (!content.value.trim()) { errorMsg.value = '내용을 입력해주세요.'; return }

  isLoading.value = true
  try {
    const formData = new FormData()
    formData.append('data', new Blob([JSON.stringify({
      category: category.value,
      title: title.value.trim(),
      content: content.value.trim(),
    })], { type: 'application/json' }))
    newFiles.value.forEach(f => formData.append('files', f))

    const res = await freeApi.create(formData)
    const id = res.data.data?.id
    toastStore.show('success', '게시글이 등록되었습니다.')
    router.push(id ? `/boards/free/${id}` : '/boards/free')
  } catch (error) {
    errorMsg.value = error.response?.data?.message || '오류가 발생했습니다.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="write-page">
    <h1 class="write-title">자유게시판 글쓰기</h1>

    <div class="form">
      <div class="field">
        <label>분류</label>
        <select v-model="category">
          <option value="HUMOR">유머</option>
          <option value="HOBBY">취미</option>
        </select>
      </div>

      <div class="field">
        <label>제목</label>
        <input v-model="title" type="text" maxlength="255" placeholder="제목을 입력하세요" />
      </div>

      <div class="field">
        <label>내용</label>
        <textarea v-model="content" rows="12" placeholder="내용을 입력하세요" />
      </div>

      <div class="field">
        <label>첨부파일 (최대 5개, 각 20MB)</label>
        <FileAttachment :maxCount="5" :maxSizeMb="20" @update:newFiles="handleNewFiles" />
      </div>

      <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>

      <div class="form-actions">
        <RouterLink to="/boards/free" class="btn-cancel">취소</RouterLink>
        <button class="btn-submit" :disabled="isLoading" @click="handleSubmit">
          {{ isLoading ? '등록 중...' : '등록' }}
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
.field input[type=text], .field select, .field textarea { padding: 10px 12px; border: 1px solid #ccc; border-radius: 4px; font-size: 0.95rem; outline: none; font-family: inherit; }
.field input[type=text]:focus, .field select:focus, .field textarea:focus { border-color: #1976d2; }
.field textarea { resize: vertical; }
.error-msg { color: #e53935; font-size: 0.85rem; }
.form-actions { display: flex; gap: 10px; justify-content: flex-end; }
.btn-cancel { padding: 10px 22px; border: 1px solid #ccc; border-radius: 4px; font-size: 0.9rem; color: #555; background: #fff; }
.btn-cancel:hover { background: #f5f5f5; }
.btn-submit { padding: 10px 22px; background: #1976d2; color: #fff; border: none; border-radius: 4px; font-size: 0.9rem; cursor: pointer; }
.btn-submit:hover:not(:disabled) { background: #1565c0; }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
