<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  images: { type: Array, required: true }, // [{ fileUrl, sortOrder }]
})

const currentIdx = ref(0)

const sorted = computed(() =>
  [...props.images].sort((a, b) => a.sortOrder - b.sortOrder)
)

function prev() {
  currentIdx.value = (currentIdx.value - 1 + sorted.value.length) % sorted.value.length
}
function next() {
  currentIdx.value = (currentIdx.value + 1) % sorted.value.length
}
</script>

<template>
  <div class="carousel" v-if="sorted.length">
    <div class="carousel-main">
      <button
        v-if="sorted.length > 1"
        class="carousel-arrow left"
        @click="prev"
      >&#8249;</button>

      <img
        :src="sorted[currentIdx].fileUrl"
        :alt="`이미지 ${currentIdx + 1}`"
        class="carousel-img"
      />

      <button
        v-if="sorted.length > 1"
        class="carousel-arrow right"
        @click="next"
      >&#8250;</button>
    </div>

    <div class="carousel-indicators" v-if="sorted.length > 1">
      <button
        v-for="(img, idx) in sorted"
        :key="idx"
        :class="['indicator-dot', { active: idx === currentIdx }]"
        @click="currentIdx = idx"
      />
    </div>
  </div>
</template>

<style scoped>
.carousel { display: flex; flex-direction: column; align-items: center; gap: 12px; }
.carousel-main {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  max-width: 700px;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  min-height: 300px;
}
.carousel-img {
  width: 100%;
  max-height: 480px;
  object-fit: contain;
  display: block;
}
.carousel-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0,0,0,0.4);
  color: #fff;
  border: none;
  width: 40px;
  height: 40px;
  font-size: 1.6rem;
  cursor: pointer;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}
.carousel-arrow.left { left: 10px; }
.carousel-arrow.right { right: 10px; }
.carousel-arrow:hover { background: rgba(0,0,0,0.7); }
.carousel-indicators { display: flex; gap: 8px; }
.indicator-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ccc;
  border: none;
  cursor: pointer;
  transition: background 0.2s;
}
.indicator-dot.active { background: #1976d2; }
</style>
