<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useModalStore } from '@/stores/modal'
import { useToastStore } from '@/stores/toast'
import BoardFilter from '@/components/common/BoardFilter.vue'
import Pagination from '@/components/common/Pagination.vue'
import galleryApi from '@/api/gallery'

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
  { value: 'FOOD', label: '음식' },
  { value: 'CELEBRITY', label: '연예인' },
]

let currentParams = {}

async function fetchList(params = {}) {
  isLoading.value = true
  currentParams = params
  try {
    const res = await galleryApi.getList(params)
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
  router.push('/boards/gallery/write')
}

onMounted(() => fetchList())
</script>

<template>
  <div class="board-page">
    <div class="board-header">
      <h1 class="board-title">갤러리</h1>
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

    <div v-else class="gallery-grid">
      <div v-if="!posts.length" class="no-data">게시글이 없습니다.</div>

      <div
        v-for="post in posts"
        :key="post.id"
        class="gallery-card"
        @click="router.push(`/boards/gallery/${post.id}`)"
      >
        <div class="card-thumb">
          <img v-if="post.thumbnailUrl" :src="post.thumbnailUrl" :alt="post.title" />
          <div v-else class="no-thumb">이미지 없음</div>
          <span v-if="post.additionalImageCount > 0" class="more-count">+{{ post.additionalImageCount }}</span>
        </div>
        <div class="card-body">
          <div class="card-category">
            <span class="badge">{{ post.categoryLabel }}</span>
          </div>
          <p class="card-title">{{ post.title }}</p>
          <p class="card-preview">{{ post.contentPreview }}</p>
          <div class="card-footer">
            <span>{{ post.authorName }}</span>
            <span>{{ post.createdAt }}</span>
            <span>조회 {{ post.viewCount }}</span>
          </div>
        </div>
      </div>
    </div>

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
.board-meta { font-size: 0.85rem; color: #666; margin-bottom: 12px; }
.loading { text-align: center; padding: 40px; color: #999; }
.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}
.no-data { grid-column: 1/-1; text-align: center; padding: 60px; color: #999; }
.gallery-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.gallery-card:hover { box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
.card-thumb {
  position: relative;
  height: 180px;
  background: #f0f0f0;
  overflow: hidden;
}
.card-thumb img { width: 100%; height: 100%; object-fit: cover; }
.no-thumb { display: flex; align-items: center; justify-content: center; height: 100%; color: #aaa; font-size: 0.85rem; }
.more-count {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0,0,0,0.6);
  color: #fff;
  font-size: 0.8rem;
  padding: 2px 8px;
  border-radius: 12px;
}
.card-body { padding: 12px; }
.card-category { margin-bottom: 6px; }
.badge { display: inline-block; padding: 2px 8px; background: #fce4ec; color: #c62828; border-radius: 12px; font-size: 0.75rem; font-weight: 600; }
.card-title { font-size: 0.95rem; font-weight: 600; color: #222; margin-bottom: 6px; }
.card-preview { font-size: 0.82rem; color: #777; line-height: 1.5; margin-bottom: 8px; overflow: hidden; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.card-footer { display: flex; gap: 10px; font-size: 0.78rem; color: #999; }
</style>
