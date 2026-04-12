<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useToastStore } from '@/stores/toast'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import noticeApi from '@/api/notice'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const toastStore = useToastStore()

const post = ref(null)
const isLoading = ref(true)
const showDeleteConfirm = ref(false)

async function fetchDetail() {
  try {
    const res = await noticeApi.getDetail(route.params.id)
    post.value = res.data.data
  } catch (error) {
    const status = error.response?.status
    const msg = error.response?.data?.message
    if (status !== 401) {
      toastStore.show('error', msg || '게시글을 불러오지 못했습니다.')
      router.push('/boards/notice')
    }
  } finally {
    isLoading.value = false
  }
}

async function handleDelete() {
  showDeleteConfirm.value = false
  try {
    await noticeApi.remove(route.params.id)
    toastStore.show('success', '게시글이 삭제되었습니다.')
    router.push('/boards/notice')
  } catch (error) {
    const msg = error.response?.data?.message
    if (error.response?.status !== 401) toastStore.show('error', msg || '오류가 발생했습니다.')
  }
}

onMounted(fetchDetail)
</script>

<template>
  <div class="detail-page">
    <div v-if="isLoading" class="loading">불러오는 중...</div>

    <template v-else-if="post">
      <div class="detail-header">
        <div class="detail-meta-top">
          <span class="badge">{{ post.categoryLabel }}</span>
          <span v-if="post.isPinned" class="badge-pinned">고정</span>
        </div>
        <h1 class="detail-title">{{ post.title }}</h1>
        <div class="detail-meta">
          <span>{{ post.authorName }}</span>
          <span>{{ post.createdAt }}</span>
          <span>조회 {{ post.viewCount }}</span>
        </div>
      </div>

      <div class="detail-content">{{ post.content }}</div>

      <div class="detail-actions">
        <RouterLink to="/boards/notice" class="btn-back">목록</RouterLink>
        <template v-if="post.isEditable">
          <RouterLink :to="`/boards/notice/modify/${post.id}`" class="btn-edit">수정</RouterLink>
          <button class="btn-delete" @click="showDeleteConfirm = true">삭제</button>
        </template>
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
.detail-header {
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 16px;
  margin-bottom: 24px;
}
.detail-meta-top { display: flex; gap: 8px; margin-bottom: 10px; }
.badge {
  display: inline-block;
  padding: 2px 10px;
  background: #e3f2fd;
  color: #1565c0;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}
.badge-pinned {
  display: inline-block;
  padding: 2px 10px;
  background: #fff3e0;
  color: #e65100;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}
.detail-title {
  font-size: 1.4rem;
  font-weight: 700;
  color: #222;
  margin-bottom: 12px;
}
.detail-meta {
  display: flex;
  gap: 16px;
  font-size: 0.85rem;
  color: #888;
}
.detail-content {
  min-height: 200px;
  font-size: 0.95rem;
  line-height: 1.8;
  color: #333;
  white-space: pre-line;
  padding: 8px 0;
  border-bottom: 1px solid #e0e0e0;
  margin-bottom: 24px;
}
.detail-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
.btn-back {
  padding: 8px 18px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.9rem;
  color: #555;
  background: #fff;
}
.btn-back:hover { background: #f5f5f5; }
.btn-edit {
  padding: 8px 18px;
  border: 1px solid #1976d2;
  border-radius: 4px;
  font-size: 0.9rem;
  color: #1976d2;
  background: #fff;
}
.btn-edit:hover { background: #e3f2fd; }
.btn-delete {
  padding: 8px 18px;
  border: none;
  border-radius: 4px;
  font-size: 0.9rem;
  color: #fff;
  background: #e53935;
  cursor: pointer;
}
.btn-delete:hover { background: #c62828; }
</style>
