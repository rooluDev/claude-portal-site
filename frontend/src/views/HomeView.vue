<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import mainApi from '@/api/main'

const router = useRouter()
const data = ref(null)
const isLoading = ref(true)

async function fetchMainData() {
  try {
    const res = await mainApi.getMainData()
    data.value = res.data.data
  } catch (e) {
    // 메인 페이지 오류는 조용히 처리
  } finally {
    isLoading.value = false
  }
}

onMounted(fetchMainData)
</script>

<template>
  <div class="home">
    <div v-if="isLoading" class="loading">불러오는 중...</div>

    <template v-else-if="data">
      <!-- 공지사항 + 자유게시판 -->
      <div class="widget-row">
        <!-- 공지사항 위젯 -->
        <div class="widget">
          <div class="widget-header">
            <h2 class="widget-title">공지사항</h2>
            <RouterLink to="/boards/notice" class="widget-more">더보기</RouterLink>
          </div>
          <ul class="widget-list">
            <li
              v-for="post in data.notices"
              :key="post.id"
              :class="{ 'pinned-item': post.isPinned }"
              @click="router.push(`/boards/notice/${post.id}`)"
            >
              <span class="item-badge notice">{{ post.categoryLabel }}</span>
              <span class="item-title">{{ post.title }}</span>
              <span class="item-date">{{ post.createdAt }}</span>
            </li>
            <li v-if="!data.notices.length" class="no-data">등록된 글이 없습니다.</li>
          </ul>
        </div>

        <!-- 자유게시판 위젯 -->
        <div class="widget">
          <div class="widget-header">
            <h2 class="widget-title">자유게시판</h2>
            <RouterLink to="/boards/free" class="widget-more">더보기</RouterLink>
          </div>
          <ul class="widget-list">
            <li
              v-for="post in data.freePosts"
              :key="post.id"
              @click="router.push(`/boards/free/${post.id}`)"
            >
              <span class="item-badge free">{{ post.categoryLabel }}</span>
              <span class="item-title">{{ post.title }}</span>
              <span class="item-date">{{ post.createdAt }}</span>
            </li>
            <li v-if="!data.freePosts.length" class="no-data">등록된 글이 없습니다.</li>
          </ul>
        </div>
      </div>

      <!-- 갤러리 위젯 -->
      <div class="widget widget-full">
        <div class="widget-header">
          <h2 class="widget-title">갤러리</h2>
          <RouterLink to="/boards/gallery" class="widget-more">더보기</RouterLink>
        </div>
        <div class="gallery-thumb-row">
          <div
            v-for="post in data.galleryPosts"
            :key="post.id"
            class="gallery-thumb"
            @click="router.push(`/boards/gallery/${post.id}`)"
          >
            <div class="thumb-img-wrap">
              <img v-if="post.thumbnailUrl" :src="post.thumbnailUrl" :alt="post.title" />
              <div v-else class="no-thumb">이미지 없음</div>
            </div>
            <p class="thumb-title">{{ post.title }}</p>
            <p class="thumb-meta">{{ post.authorName }} · {{ post.createdAt }}</p>
          </div>
          <div v-if="!data.galleryPosts.length" class="no-data">등록된 글이 없습니다.</div>
        </div>
      </div>

      <!-- 문의게시판 위젯 -->
      <div class="widget widget-full">
        <div class="widget-header">
          <h2 class="widget-title">문의게시판</h2>
          <RouterLink to="/boards/inquiry" class="widget-more">더보기</RouterLink>
        </div>
        <ul class="widget-list">
          <li
            v-for="post in data.inquiries"
            :key="post.id"
            @click="router.push(`/boards/inquiry/${post.id}`)"
          >
            <span v-if="post.isSecret" class="lock-icon">🔒</span>
            <span class="item-title">{{ post.title }}</span>
            <span :class="['item-status', post.answerStatus === 'ANSWERED' ? 'answered' : 'pending']">
              {{ post.answerStatusLabel }}
            </span>
            <span class="item-date">{{ post.createdAt }}</span>
          </li>
          <li v-if="!data.inquiries.length" class="no-data">등록된 글이 없습니다.</li>
        </ul>
      </div>
    </template>
  </div>
</template>

<style scoped>
.home { display: flex; flex-direction: column; gap: 28px; }
.loading { text-align: center; padding: 80px; color: #999; }
.widget-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}
.widget {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
}
.widget-full { }
.widget-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  border-bottom: 2px solid #1976d2;
  padding-bottom: 10px;
}
.widget-title { font-size: 1.05rem; font-weight: 700; color: #1976d2; }
.widget-more { font-size: 0.82rem; color: #888; }
.widget-more:hover { color: #1976d2; }
.widget-list { list-style: none; display: flex; flex-direction: column; gap: 2px; }
.widget-list li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 4px;
  border-bottom: 1px solid #f5f5f5;
  cursor: pointer;
  font-size: 0.88rem;
  color: #444;
}
.widget-list li:hover { background: #f9f9f9; }
.pinned-item { background: #fafafa; font-weight: 600; }
.item-badge {
  display: inline-block;
  padding: 1px 7px;
  border-radius: 10px;
  font-size: 0.75rem;
  font-weight: 600;
  white-space: nowrap;
}
.item-badge.notice { background: #e3f2fd; color: #1565c0; }
.item-badge.free { background: #e8f5e9; color: #2e7d32; }
.item-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.item-date { font-size: 0.78rem; color: #aaa; white-space: nowrap; }
.item-status {
  display: inline-block;
  padding: 1px 7px;
  border-radius: 10px;
  font-size: 0.75rem;
  font-weight: 600;
  white-space: nowrap;
}
.item-status.answered { background: #e8f5e9; color: #2e7d32; }
.item-status.pending { background: #fff3e0; color: #e65100; }
.lock-icon { font-size: 0.8rem; }
.no-data { color: #bbb; font-size: 0.85rem; padding: 12px 4px; }

/* 갤러리 썸네일 */
.gallery-thumb-row {
  display: flex;
  gap: 16px;
  overflow-x: auto;
}
.gallery-thumb {
  flex: 0 0 200px;
  cursor: pointer;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  overflow: hidden;
  transition: box-shadow 0.2s;
}
.gallery-thumb:hover { box-shadow: 0 3px 10px rgba(0,0,0,0.1); }
.thumb-img-wrap {
  height: 140px;
  background: #f0f0f0;
  overflow: hidden;
}
.thumb-img-wrap img { width: 100%; height: 100%; object-fit: cover; }
.no-thumb { display: flex; align-items: center; justify-content: center; height: 100%; color: #bbb; font-size: 0.8rem; }
.thumb-title { font-size: 0.88rem; font-weight: 600; padding: 8px 8px 2px; color: #333; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.thumb-meta { font-size: 0.75rem; color: #999; padding: 0 8px 8px; }
</style>
