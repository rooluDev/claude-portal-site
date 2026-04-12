<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import CommentList from '@/components/free/CommentList.vue'
import CommentInput from '@/components/free/CommentInput.vue'
import freeApi from '@/api/free'
import commentApi from '@/api/comment'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const toastStore = useToastStore()

const post = ref(null)
const comments = ref([])
const isLoading = ref(true)
const showDeleteConfirm = ref(false)

const postId = Number(route.params.id)

async function fetchDetail() {
  try {
    const [postRes, commentRes] = await Promise.all([
      freeApi.getDetail(postId),
      commentApi.getComments('FREE', postId),
    ])
    post.value = postRes.data.data
    comments.value = commentRes.data.data
  } catch (error) {
    const status = error.response?.status
    const msg = error.response?.data?.message
    if (status !== 401) {
      toastStore.show('error', msg || '게시글을 불러오지 못했습니다.')
      router.push('/boards/free')
    }
  } finally {
    isLoading.value = false
  }
}

async function handleDelete() {
  showDeleteConfirm.value = false
  try {
    await freeApi.remove(postId)
    toastStore.show('success', '게시글이 삭제되었습니다.')
    router.push('/boards/free')
  } catch (error) {
    const msg = error.response?.data?.message
    if (error.response?.status !== 401) toastStore.show('error', msg || '오류가 발생했습니다.')
  }
}

function handleCommentCreated(newComment) {
  comments.value.push(newComment)
}

function handleCommentDeleted(id) {
  comments.value = comments.value.filter(c => c.id !== id)
}

onMounted(fetchDetail)
</script>

<template>
  <div class="detail-page">
    <div v-if="isLoading" class="loading">불러오는 중...</div>

    <template v-else-if="post">
      <div class="detail-header">
        <div class="detail-badge-row">
          <span class="badge">{{ post.categoryLabel }}</span>
        </div>
        <h1 class="detail-title">{{ post.title }}</h1>
        <div class="detail-meta">
          <span>{{ post.authorName }}</span>
          <span>{{ post.createdAt }}</span>
          <span>조회 {{ post.viewCount }}</span>
        </div>
      </div>

      <!-- 첨부파일 -->
      <div v-if="post.attachments && post.attachments.length" class="attachments">
        <p class="attach-label">첨부파일</p>
        <div v-for="att in post.attachments" :key="att.id" class="attach-item">
          <a :href="att.fileUrl" download>{{ att.originalName }}</a>
        </div>
      </div>

      <div class="detail-content">{{ post.content }}</div>

      <div class="detail-actions">
        <RouterLink to="/boards/free" class="btn-back">목록</RouterLink>
        <template v-if="post.isEditable">
          <RouterLink :to="`/boards/free/modify/${post.id}`" class="btn-edit">수정</RouterLink>
          <button class="btn-delete" @click="showDeleteConfirm = true">삭제</button>
        </template>
      </div>

      <!-- 댓글 -->
      <div class="comment-section">
        <h3 class="comment-heading">댓글 {{ comments.length }}개</h3>
        <CommentInput board-type="FREE" :post-id="postId" @created="handleCommentCreated" />
        <CommentList :comments="comments" @deleted="handleCommentDeleted" />
      </div>
    </template>

    <ConfirmDialog
      v-if="showDeleteConfirm"
      message="게시글을 삭제하시겠습니까?"
      @confirm="handleDelete"
      @cancel="showDeleteConfirm = false"
    />
  </div>
</template>

<style scoped>
.detail-page { max-width: 860px; margin: 0 auto; }
.loading { text-align: center; padding: 60px; color: #999; }
.detail-header { border-bottom: 2px solid #e0e0e0; padding-bottom: 16px; margin-bottom: 20px; }
.detail-badge-row { margin-bottom: 10px; }
.badge { display: inline-block; padding: 2px 10px; background: #e8f5e9; color: #2e7d32; border-radius: 12px; font-size: 0.8rem; font-weight: 600; }
.detail-title { font-size: 1.4rem; font-weight: 700; color: #222; margin-bottom: 12px; }
.detail-meta { display: flex; gap: 16px; font-size: 0.85rem; color: #888; }
.attachments { margin-bottom: 16px; padding: 12px 16px; background: #f5f5f5; border-radius: 6px; }
.attach-label { font-size: 0.85rem; font-weight: 600; color: #555; margin-bottom: 8px; }
.attach-item a { font-size: 0.88rem; color: #1976d2; text-decoration: underline; }
.detail-content {
  min-height: 200px;
  font-size: 0.95rem;
  line-height: 1.8;
  color: #333;
  white-space: pre-line;
  padding: 8px 0;
  border-bottom: 1px solid #e0e0e0;
  margin-bottom: 20px;
}
.detail-actions { display: flex; gap: 8px; justify-content: flex-end; margin-bottom: 32px; }
.btn-back { padding: 8px 18px; border: 1px solid #ccc; border-radius: 4px; font-size: 0.9rem; color: #555; background: #fff; }
.btn-back:hover { background: #f5f5f5; }
.btn-edit { padding: 8px 18px; border: 1px solid #1976d2; border-radius: 4px; font-size: 0.9rem; color: #1976d2; background: #fff; }
.btn-edit:hover { background: #e3f2fd; }
.btn-delete { padding: 8px 18px; border: none; border-radius: 4px; font-size: 0.9rem; color: #fff; background: #e53935; cursor: pointer; }
.btn-delete:hover { background: #c62828; }
.comment-section { border-top: 2px solid #e0e0e0; padding-top: 20px; }
.comment-heading { font-size: 1rem; font-weight: 700; margin-bottom: 16px; color: #444; }
</style>
