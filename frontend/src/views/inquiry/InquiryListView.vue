<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useModalStore } from '@/stores/modal'
import { useToastStore } from '@/stores/toast'
import BoardFilter from '@/components/common/BoardFilter.vue'
import Pagination from '@/components/common/Pagination.vue'
import inquiryApi from '@/api/inquiry'

const router = useRouter()
const authStore = useAuthStore()
const modalStore = useModalStore()
const toastStore = useToastStore()

const posts = ref([])
const totalPages = ref(0)
const totalCount = ref(0)
const currentPage = ref(1)
const isLoading = ref(false)
const myOnly = ref(false)

const CATEGORY_OPTIONS = [
  { value: 'PENDING', label: '대기중' },
  { value: 'ANSWERED', label: '답변완료' },
]

let currentParams = {}

async function fetchList(params = {}) {
  isLoading.value = true
  currentParams = params
  try {
    const res = await inquiryApi.getList({ ...params, my: myOnly.value })
    const data = res.data.data
    posts.value = data.content
    totalPages.value = data.totalPages
    totalCount.value = data.totalCount
    currentPage.value = data.currentPage
  } catch (error) {
    const status = error.response?.status
    const msg = error.response?.data?.message
    if (status !== 401) toastStore.show('error', msg || '목록을 불러오는 중 오류가 발생했습니다.')
  } finally {
    isLoading.value = false
  }
}

function handleSearch(params) { fetchList(params) }
function handlePageChange(page) { fetchList({ ...currentParams, pageNum: page }) }

function toggleMyOnly() {
  if (!authStore.isLoggedIn) { modalStore.openLoginModal(); return }
  myOnly.value = !myOnly.value
  fetchList(currentParams)
}

function handleRowClick(post) {
  if (post.isSecret) {
    if (!authStore.isLoggedIn) { modalStore.openLoginModal(); return }
    // 목록에서는 타인 비밀글 클릭 시 경고
    // 실제 접근 제어는 상세 API가 담당
  }
  router.push(`/boards/inquiry/${post.id}`)
}

function handleWrite() {
  if (!authStore.isLoggedIn) { modalStore.openLoginModal(); return }
  router.push('/boards/inquiry/write')
}

onMounted(() => fetchList())
</script>

<template>
  <div class="board-page">
    <div class="board-header">
      <h1 class="board-title">문의게시판</h1>
      <button class="btn-write" @click="handleWrite">글쓰기</button>
    </div>

    <BoardFilter
      :categoryOptions="CATEGORY_OPTIONS"
      searchPlaceholder="제목, 내용, 작성자 검색"
      @search="handleSearch"
    />

    <div class="board-meta-row">
      <span class="board-meta">총 {{ totalCount }}건</span>
      <label class="my-checkbox">
        <input type="checkbox" :checked="myOnly" @change="toggleMyOnly" />
        나의 문의내역
      </label>
    </div>

    <div v-if="isLoading" class="loading">불러오는 중...</div>

    <table v-else class="board-table">
      <thead>
        <tr>
          <th class="col-num">번호</th>
          <th class="col-title">제목</th>
          <th class="col-status">답변상태</th>
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
          @click="handleRowClick(post)"
          style="cursor:pointer"
        >
          <td class="col-num">{{ post.id }}</td>
          <td class="col-title title-cell">
            <span class="lock-icon" v-if="post.isSecret">🔒</span>
            {{ post.title }}
          </td>
          <td class="col-status">
            <span :class="['status-badge', post.answerStatus === 'ANSWERED' ? 'answered' : 'pending']">
              {{ post.answerStatusLabel }}
            </span>
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
.btn-write { padding: 8px 18px; background: #1976d2; color: #fff; border: none; border-radius: 4px; font-size: 0.9rem; cursor: pointer; }
.btn-write:hover { background: #1565c0; }
.board-meta-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.board-meta { font-size: 0.85rem; color: #666; }
.my-checkbox { display: flex; align-items: center; gap: 6px; font-size: 0.88rem; color: #555; cursor: pointer; }
.loading { text-align: center; padding: 40px; color: #999; }
.board-table { width: 100%; border-collapse: collapse; font-size: 0.9rem; }
.board-table th, .board-table td { padding: 12px 10px; border-bottom: 1px solid #e0e0e0; text-align: center; }
.board-table th { background: #f5f5f5; font-weight: 600; color: #444; }
.col-title { text-align: left; }
.title-cell { text-align: left; }
.lock-icon { margin-right: 4px; font-size: 0.85rem; }
.col-num { width: 70px; }
.col-status { width: 90px; }
.col-author { width: 100px; }
.col-date { width: 120px; }
.col-view { width: 70px; }
.status-badge { display: inline-block; padding: 2px 8px; border-radius: 12px; font-size: 0.78rem; font-weight: 600; }
.status-badge.answered { background: #e8f5e9; color: #2e7d32; }
.status-badge.pending { background: #fff3e0; color: #e65100; }
.no-data { padding: 40px; color: #999; }
</style>
