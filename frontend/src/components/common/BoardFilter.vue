<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  showCategory: { type: Boolean, default: true },
  categoryOptions: { type: Array, default: () => [] },
  searchPlaceholder: { type: String, default: '검색어를 입력하세요' },
  showOrderCategory: { type: Boolean, default: false },
  initialParams: { type: Object, default: () => ({}) },
})
const emit = defineEmits(['search'])

function today() {
  return new Date().toISOString().slice(0, 10)
}
function oneYearAgo() {
  const d = new Date()
  d.setFullYear(d.getFullYear() - 1)
  return d.toISOString().slice(0, 10)
}

const startDate = ref(props.initialParams.startDate || oneYearAgo())
const endDate = ref(props.initialParams.endDate || today())
const category = ref(props.initialParams.category || '')
const searchText = ref(props.initialParams.searchText || '')
const pageSize = ref(props.initialParams.pageSize || 10)
const orderValue = ref(props.initialParams.orderValue || 'createdAt')
const orderDirection = ref(props.initialParams.orderDirection || 'desc')

const orderOptions = [
  { value: 'createdAt', label: '등록일' },
  { value: 'title', label: '제목' },
  { value: 'viewCount', label: '조회수' },
  ...(props.showOrderCategory ? [{ value: 'category', label: '카테고리' }] : []),
]

function handleSearch() {
  emit('search', {
    startDate: startDate.value,
    endDate: endDate.value,
    category: category.value || undefined,
    searchText: searchText.value || undefined,
    pageSize: pageSize.value,
    orderValue: orderValue.value,
    orderDirection: orderDirection.value,
    pageNum: 1,
  })
}
</script>

<template>
  <div class="board-filter">
    <div class="filter-row">
      <div class="filter-group">
        <label>기간</label>
        <input type="date" v-model="startDate" class="input-date" />
        <span class="date-sep">~</span>
        <input type="date" v-model="endDate" class="input-date" />
      </div>

      <div v-if="showCategory && categoryOptions.length" class="filter-group">
        <label>카테고리</label>
        <select v-model="category" class="input-select">
          <option value="">전체</option>
          <option v-for="opt in categoryOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
      </div>

      <div class="filter-group search-group">
        <input
          v-model="searchText"
          type="text"
          :placeholder="searchPlaceholder"
          class="input-search"
          @keyup.enter="handleSearch"
        />
        <button class="btn-search" @click="handleSearch">검색</button>
      </div>
    </div>

    <div class="filter-row filter-row-right">
      <div class="filter-group">
        <label>개씩 보기</label>
        <select v-model.number="pageSize" class="input-select-sm">
          <option :value="10">10개</option>
          <option :value="20">20개</option>
          <option :value="30">30개</option>
        </select>
      </div>
      <div class="filter-group">
        <label>정렬</label>
        <select v-model="orderValue" class="input-select-sm">
          <option v-for="opt in orderOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
        <select v-model="orderDirection" class="input-select-sm">
          <option value="desc">내림차순</option>
          <option value="asc">오름차순</option>
        </select>
      </div>
    </div>
  </div>
</template>

<style scoped>
.board-filter {
  background: #f9f9f9;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  padding: 16px 20px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.filter-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}
.filter-row-right {
  justify-content: flex-end;
}
.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}
.filter-group label {
  font-size: 0.85rem;
  color: #666;
  white-space: nowrap;
}
.date-sep { color: #999; }
.input-date {
  padding: 6px 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.88rem;
  outline: none;
}
.input-select, .input-select-sm {
  padding: 6px 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.88rem;
  outline: none;
  background: #fff;
}
.search-group { flex: 1; }
.input-search {
  flex: 1;
  padding: 7px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 0.9rem;
  outline: none;
  min-width: 200px;
}
.input-search:focus { border-color: #1976d2; }
.btn-search {
  padding: 7px 18px;
  background: #1976d2;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  white-space: nowrap;
}
.btn-search:hover { background: #1565c0; }
</style>
