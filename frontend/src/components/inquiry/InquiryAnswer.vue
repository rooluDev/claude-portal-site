<script setup>
import { ref } from 'vue'
import { useToastStore } from '@/stores/toast'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import inquiryApi from '@/api/inquiry'

const props = defineProps({
  inquiryId: { type: Number, required: true },
  answer: { type: Object, default: null },
  answerStatus: { type: String, required: true },
  isAdmin: { type: Boolean, default: false },
})
const emit = defineEmits(['answer-changed'])

const toastStore = useToastStore()
const editContent = ref('')
const isEditing = ref(false)
const showDeleteConfirm = ref(false)
const isLoading = ref(false)

// 답변 등록
const newContent = ref('')

async function handleCreate() {
  if (!newContent.value.trim()) {
    toastStore.show('error', '답변 내용을 입력해주세요.')
    return
  }
  isLoading.value = true
  try {
    await inquiryApi.createAnswer(props.inquiryId, { content: newContent.value.trim() })
    toastStore.show('success', '답변이 등록되었습니다.')
    newContent.value = ''
    emit('answer-changed')
  } catch (error) {
    const msg = error.response?.data?.message
    if (error.response?.status !== 401) toastStore.show('error', msg || '오류가 발생했습니다.')
  } finally {
    isLoading.value = false
  }
}

function startEdit() {
  editContent.value = props.answer?.content || ''
  isEditing.value = true
}

async function handleUpdate() {
  if (!editContent.value.trim()) {
    toastStore.show('error', '답변 내용을 입력해주세요.')
    return
  }
  isLoading.value = true
  try {
    await inquiryApi.updateAnswer(props.inquiryId, { content: editContent.value.trim() })
    toastStore.show('success', '답변이 수정되었습니다.')
    isEditing.value = false
    emit('answer-changed')
  } catch (error) {
    const msg = error.response?.data?.message
    if (error.response?.status !== 401) toastStore.show('error', msg || '오류가 발생했습니다.')
  } finally {
    isLoading.value = false
  }
}

async function handleDelete() {
  showDeleteConfirm.value = false
  isLoading.value = true
  try {
    await inquiryApi.removeAnswer(props.inquiryId)
    toastStore.show('success', '답변이 삭제되었습니다.')
    emit('answer-changed')
  } catch (error) {
    const msg = error.response?.data?.message
    if (error.response?.status !== 401) toastStore.show('error', msg || '오류가 발생했습니다.')
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="inquiry-answer">
    <div class="answer-header">
      <span class="answer-label">답변</span>
    </div>

    <!-- PENDING: 관리자 답변 입력 -->
    <template v-if="answerStatus === 'PENDING' && isAdmin">
      <textarea
        v-model="newContent"
        class="answer-textarea"
        placeholder="답변 내용을 입력하세요."
        rows="5"
      />
      <button class="btn-primary" :disabled="isLoading" @click="handleCreate">답변 등록</button>
    </template>

    <!-- PENDING: 일반 사용자 -->
    <template v-else-if="answerStatus === 'PENDING'">
      <p class="no-answer">아직 답변이 등록되지 않았습니다.</p>
    </template>

    <!-- ANSWERED -->
    <template v-else-if="answerStatus === 'ANSWERED' && answer">
      <div v-if="!isEditing">
        <div class="answer-meta">
          <span class="admin-name">{{ answer.adminName }}</span>
          <span class="answer-date">{{ answer.updatedAt }}</span>
        </div>
        <p class="answer-content">{{ answer.content }}</p>

        <div v-if="isAdmin" class="answer-actions">
          <button class="btn-edit" @click="startEdit">답변 수정</button>
          <button class="btn-delete" @click="showDeleteConfirm = true">답변 삭제</button>
        </div>
      </div>

      <!-- 수정 모드 -->
      <div v-else>
        <textarea
          v-model="editContent"
          class="answer-textarea"
          rows="5"
        />
        <div class="answer-actions">
          <button class="btn-cancel" @click="isEditing = false">취소</button>
          <button class="btn-primary" :disabled="isLoading" @click="handleUpdate">수정 완료</button>
        </div>
      </div>
    </template>

    <ConfirmDialog
      v-if="showDeleteConfirm"
      message="답변을 삭제하시겠습니까? 문의 상태가 대기중으로 변경됩니다."
      @confirm="handleDelete"
      @cancel="showDeleteConfirm = false"
    />
  </div>
</template>

<style scoped>
.inquiry-answer {
  background: #f5f9ff;
  border: 1px solid #c5d8f0;
  border-radius: 8px;
  padding: 20px;
  margin-top: 24px;
}
.answer-header { margin-bottom: 16px; }
.answer-label {
  font-weight: 700;
  font-size: 1rem;
  color: #1976d2;
}
.no-answer { color: #999; font-size: 0.9rem; }
.answer-meta {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 10px;
}
.admin-name { font-weight: 600; font-size: 0.9rem; color: #333; }
.answer-date { font-size: 0.82rem; color: #999; }
.answer-content {
  font-size: 0.93rem;
  color: #444;
  line-height: 1.7;
  white-space: pre-line;
}
.answer-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #b0c4e0;
  border-radius: 4px;
  font-size: 0.92rem;
  resize: vertical;
  outline: none;
  font-family: inherit;
  box-sizing: border-box;
  background: #fff;
  margin-bottom: 10px;
}
.answer-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 12px;
}
.btn-primary {
  padding: 8px 18px;
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}
.btn-primary:hover:not(:disabled) { background: #1565c0; }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-edit {
  padding: 6px 14px;
  background: #fff;
  border: 1px solid #1976d2;
  color: #1976d2;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
}
.btn-edit:hover { background: #e3f2fd; }
.btn-delete {
  padding: 6px 14px;
  background: #fff;
  border: 1px solid #e53935;
  color: #e53935;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
}
.btn-delete:hover { background: #ffebee; }
.btn-cancel {
  padding: 6px 14px;
  border: 1px solid #bbb;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
}
</style>
