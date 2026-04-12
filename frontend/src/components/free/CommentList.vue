<script setup>
import { ref } from 'vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import { useToastStore } from '@/stores/toast'
import commentApi from '@/api/comment'

const props = defineProps({
  comments: { type: Array, required: true },
})
const emit = defineEmits(['deleted'])

const toastStore = useToastStore()
const showConfirm = ref(false)
const targetId = ref(null)

function requestDelete(id) {
  targetId.value = id
  showConfirm.value = true
}

async function confirmDelete() {
  showConfirm.value = false
  try {
    await commentApi.remove(targetId.value)
    toastStore.show('success', '댓글이 삭제되었습니다.')
    emit('deleted', targetId.value)
  } catch (error) {
    const msg = error.response?.data?.message
    if (error.response?.status !== 401) {
      toastStore.show('error', msg || '오류가 발생했습니다.')
    }
  }
}
</script>

<template>
  <div class="comment-list">
    <div v-if="!comments.length" class="no-comment">등록된 댓글이 없습니다.</div>
    <div
      v-for="comment in comments"
      :key="comment.id"
      class="comment-item"
    >
      <div class="comment-meta">
        <span class="comment-author">{{ comment.authorName }}</span>
        <span class="comment-date">{{ comment.createdAt }}</span>
        <button
          v-if="comment.isDeletable"
          class="btn-delete-comment"
          @click="requestDelete(comment.id)"
        >삭제</button>
      </div>
      <p class="comment-content">{{ comment.content }}</p>
    </div>

    <ConfirmDialog
      v-if="showConfirm"
      message="댓글을 삭제하시겠습니까?"
      @confirm="confirmDelete"
      @cancel="showConfirm = false"
    />
  </div>
</template>

<style scoped>
.comment-list { display: flex; flex-direction: column; gap: 1px; }
.no-comment { padding: 20px; text-align: center; color: #999; font-size: 0.9rem; }
.comment-item {
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}
.comment-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.comment-author {
  font-weight: 600;
  font-size: 0.9rem;
  color: #333;
}
.comment-date {
  font-size: 0.82rem;
  color: #999;
}
.btn-delete-comment {
  margin-left: auto;
  font-size: 0.8rem;
  color: #e53935;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}
.btn-delete-comment:hover { text-decoration: underline; }
.comment-content {
  font-size: 0.92rem;
  color: #444;
  line-height: 1.6;
  white-space: pre-line;
}
</style>
