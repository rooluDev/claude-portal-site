<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useModalStore } from '@/stores/modal'
import { useToastStore } from '@/stores/toast'
import BoardFilter from '@/components/common/BoardFilter.vue'
import Pagination from '@/components/common/Pagination.vue'
import freeApi from '@/api/free'

const router = useRouter()
const authStore = useAuthStore()
const modalStore = useModalStore()
const toastStore = useToastStore()

const posts = ref([])
const totalPages = ref(0)
const totalCount = ref(0)
const currentPage = ref(1)
const isLoading = ref(false)

const CATEGORY_OPTIONS = [
  { value: 'HUMOR', label: '유머' },
  { value: 'HOBBY', label: '취미' },
]

let currentParams = {}

async function fetchList(params = {}) {
  isLoading.value = true
  currentParams = params
  try {
    const res = await freeApi.getList(params)
    const data = res.data.data
    posts.value = data.content
    totalPages.value = data.totalPages
    totalCount.value = data.totalCount
    currentPage.value = data.currentPage
  } catch (error) {
    toastStore.show('error', '목록을 불러오는 중 오류가 발생했습니다.')
  } finally {
    isLoading.value = false
  }
}

function handleSearch(params) { fetchList(params) }
function handlePageChange(page) { fetchList({ ...currentParams, pageNum: page }) }

function handleWrite() {
  if (!authStore.isLoggedIn) { modalStore.openLoginModal(); return }
  router.push('/boards/free/write')
}

onMounted(() => fetchList())
</script>

<template>
  <div class="board-page">
    <div class="board-header">
      <h1 class="board-title">자유게시판</h1>
      <button class="btn-write" @click="handleWrite">글쓰기</button>
    </div>

    <BoardFilter
      :categoryOptions="CATEGORY_OPTIONS"
      searchPlaceholder="제목, 내용, 작성자 검색"
      :showOrderCategory="true"
      @search="handleSearch"
    />

    <div class="board-meta">총 {{ totalCount }}건</div>

    <div v-if="isLoading" class="loading">불러오는 중...</div>

    <table v-else class="board-table">
      <thead>
        <tr>
          <th class="col-num">번호</th>
          <th class="col-category">분류</th>
          <th class="col-title">제목</th>
          <th class="col-author">작성자</th>
          <th class="col-date">등록일</th>
          <th class="col-view">조회</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="!posts.length">
          <td colspan="6" class="no-data">게시글이 없습니다.</td>
        </tr>
        <tr
          v-for="post in posts"
          :key="post.id"
          @click="router.push(`/boards/free/${post.id}`)"
          style="cursor:pointer"
        >
          <td class="col-num">{{ post.id }}</td>
          <td class="col-category">
            <span class="badge">{{ post.categoryLabel }}</span>
          </td>
          <td class="col-title title-cell">
            {{ post.title }}
            <span v-if="post.commentCount" class="comment-count">[{{ post.commentCount }}]</span>
            <span v-if="post.hasAttachment" class="attach-icon" title="첨부파일">📎</span>
          </td>
          <td class="col-author">{{ post.authorName }}</td>
          <td class="col-date">{{ post.createdAt }}</td>
          <td class="col-view">{{ post.viewCount }}</td>
        </tr>
      </tbody>
    </table>

    <Pagination
      :totalPages="totalPages"
      :currentPage="currentPage"
      @page-change="handlePageChange"
    />
  </div>
</template>

<style scoped>
.board-page { }
.board-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.board-title { font-size: 1.5rem; font-weight: 700; }
.btn-write {
  padding: 8px 18px;
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 0.9rem;
  cursor: pointer;
}
.btn-write:hover { background: #1565c0; }
.board-meta { font-size: 0.85rem; color: #666; margin-bottom: 8px; }
.loading { text-align: center; padding: 40px; color: #999; }
.board-table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
.board-table th, .board-table td { padding: 12px 10px; border-bottom: 1px solid #e0e0e0; text-align: center; }
.board-table th { background: #f5f5f5; font-weight: 600; color: #444; }
.col-title { text-align: left; }
.title-cell { text-align: left; }
.col-num { width: 70px; }
.col-category { width: 80px; }
.col-author { width: 100px; }
.col-date { width: 120px; }
.col-view { width: 70px; }
.badge { display: inline-block; padding: 2px 8px; background: #e8f5e9; color: #2e7d32; border-radius: 12px; font-size: 0.78rem; font-weight: 600; }
.comment-count { color: #1976d2; font-size: 0.85rem; margin-left: 4px; }
.attach-icon { margin-left: 4px; font-size: 0.9rem; }
.no-data { padding: 40px; color: #999; }
</style>
