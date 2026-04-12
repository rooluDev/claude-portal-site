<script setup>
import { computed } from 'vue'

const props = defineProps({
  totalPages: { type: Number, required: true },
  currentPage: { type: Number, required: true },
})
const emit = defineEmits(['page-change'])

const GROUP_SIZE = 10

const currentGroup = computed(() => Math.floor((props.currentPage - 1) / GROUP_SIZE))

const pages = computed(() => {
  const start = currentGroup.value * GROUP_SIZE + 1
  const end = Math.min(start + GROUP_SIZE - 1, props.totalPages)
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

const hasPrev = computed(() => currentGroup.value > 0)
const hasNext = computed(() => (currentGroup.value + 1) * GROUP_SIZE < props.totalPages)

function prevGroup() {
  emit('page-change', currentGroup.value * GROUP_SIZE)
}
function nextGroup() {
  emit('page-change', (currentGroup.value + 1) * GROUP_SIZE + 1)
}
</script>

<template>
  <div class="pagination" v-if="totalPages > 1">
    <button class="pg-btn" :disabled="!hasPrev" @click="prevGroup">&#9664;</button>

    <button
      v-for="p in pages"
      :key="p"
      :class="['pg-btn', { active: p === currentPage }]"
      @click="emit('page-change', p)"
    >
      {{ p }}
    </button>

    <button class="pg-btn" :disabled="!hasNext" @click="nextGroup">&#9654;</button>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  justify-content: center;
  gap: 4px;
  margin-top: 24px;
}
.pg-btn {
  min-width: 34px;
  height: 34px;
  padding: 0 8px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  color: #555;
  transition: all 0.15s;
}
.pg-btn:hover:not(:disabled) {
  background: #e3f2fd;
  border-color: #1976d2;
  color: #1976d2;
}
.pg-btn.active {
  background: #1976d2;
  border-color: #1976d2;
  color: #fff;
  font-weight: 600;
}
.pg-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
</style>
