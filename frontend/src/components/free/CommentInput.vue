<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useModalStore } from '@/stores/modal'
import { useToastStore } from '@/stores/toast'
import commentApi from '@/api/comment'

const props = defineProps({
  boardType: { type: String, required: true },
  postId: { type: Number, required: true },
})
const emit = defineEmits(['created'])

const authStore = useAuthStore()
const modalStore = useModalStore()
const toastStore = useToastStore()

const content = ref('')
const isLoading = ref(false)

async function handleSubmit() {
  if (!authStore.isLoggedIn) {
    modalStore.openLoginModal()
    return
  }
  if (!content.value.trim()) {
    toastStore.show('error', '댓글 내용을 입력해주세요.')
    return
  }
  isLoading.value = true
  try {
    const res = await commentApi.create({
      boardType: props.boardType,
      postId: props.postId,
      content: content.value.trim(),
    })
    content.value = ''
    emit('created', res.data.data)
  } catch (error) {
    const msg = error.response?.data?.message
    if (error.response?.status !== 401) {
      toastStore.show('error', msg || '오류가 발생했습니다.')
    }
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="comment-input">
    <textarea
      v-model="content"
      class="comment-textarea"
      placeholder="댓글을 입력하세요. (최대 1000자)"
      maxlength="1000"
      rows="3"
    />
    <div class="comment-input-actions">
      <span class="char-count">{{ content.length }}/1000</span>
      <button class="btn-submit" :disabled="isLoading" @click="handleSubmit">
        {{ isLoading ? '등록 중...' : '댓글 등록' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.comment-input { display: flex; flex-direction: column; gap: 8px; }
.comment-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.92rem;
  resize: vertical;
  outline: none;
  font-family: inherit;
  box-sizing: border-box;
}
.comment-textarea:focus { border-color: #1976d2; }
.comment-input-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
}
.char-count { font-size: 0.8rem; color: #999; }
.btn-submit {
  padding: 8px 18px;
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}
.btn-submit:hover:not(:disabled) { background: #1565c0; }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
</style>
