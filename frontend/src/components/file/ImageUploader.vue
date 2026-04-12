<script setup>
import { ref, computed } from 'vue'

const ALLOWED_EXT = ['jpg', 'jpeg', 'png', 'gif', 'webp']

const props = defineProps({
  existingImages: { type: Array, default: () => [] },
  maxCount: { type: Number, default: 10 },
  maxSizeMb: { type: Number, default: 10 },
})

const emit = defineEmits(['update:newFiles', 'update:deleteIds'])

const newFiles = ref([])
const newPreviews = ref([])
const pendingDeleteIds = ref([])
const errorMsg = ref('')

const remainingExisting = computed(() =>
  props.existingImages.filter(img => !pendingDeleteIds.value.includes(img.id))
)
const totalCount = computed(() => remainingExisting.value.length + newFiles.value.length)

function getExt(filename) {
  return filename.split('.').pop().toLowerCase()
}

function handleFileChange(e) {
  errorMsg.value = ''
  const selected = Array.from(e.target.files)

  for (const file of selected) {
    if (!ALLOWED_EXT.includes(getExt(file.name))) {
      errorMsg.value = '이미지 파일만 업로드할 수 있습니다. (jpg, jpeg, png, gif, webp)'
      e.target.value = ''
      return
    }
    if (file.size > props.maxSizeMb * 1024 * 1024) {
      errorMsg.value = `파일 크기는 ${props.maxSizeMb}MB를 초과할 수 없습니다.`
      e.target.value = ''
      return
    }
  }
  if (totalCount.value + selected.length > props.maxCount) {
    errorMsg.value = `이미지는 최대 ${props.maxCount}장까지 업로드할 수 있습니다.`
    e.target.value = ''
    return
  }

  selected.forEach(file => {
    newFiles.value.push(file)
    const reader = new FileReader()
    reader.onload = (ev) => newPreviews.value.push(ev.target.result)
    reader.readAsDataURL(file)
  })
  emit('update:newFiles', [...newFiles.value])
  e.target.value = ''
}

function removeNew(idx) {
  newFiles.value.splice(idx, 1)
  newPreviews.value.splice(idx, 1)
  emit('update:newFiles', [...newFiles.value])
}

function removeExisting(id) {
  pendingDeleteIds.value.push(id)
  emit('update:deleteIds', [...pendingDeleteIds.value])
}
</script>

<template>
  <div class="image-uploader">
    <div class="image-grid">
      <div
        v-for="img in remainingExisting"
        :key="'ex-' + img.id"
        class="image-thumb"
      >
        <img :src="img.fileUrl" :alt="img.originalName" />
        <button class="btn-remove-img" @click="removeExisting(img.id)">×</button>
      </div>

      <div
        v-for="(preview, idx) in newPreviews"
        :key="'new-' + idx"
        class="image-thumb"
      >
        <img :src="preview" :alt="newFiles[idx]?.name" />
        <button class="btn-remove-img" @click="removeNew(idx)">×</button>
      </div>

      <label v-if="totalCount < maxCount" class="add-image-btn">
        <span class="plus-icon">+</span>
        <span class="count-txt">{{ totalCount }}/{{ maxCount }}</span>
        <input type="file" multiple hidden accept="image/jpeg,image/png,image/gif,image/webp" @change="handleFileChange" />
      </label>
    </div>

    <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
  </div>
</template>

<style scoped>
.image-uploader { display: flex; flex-direction: column; gap: 8px; }
.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.image-thumb {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #ddd;
}
.image-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.btn-remove-img {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(0,0,0,0.5);
  color: #fff;
  border: none;
  cursor: pointer;
  font-size: 0.9rem;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.add-image-btn {
  width: 100px;
  height: 100px;
  border: 2px dashed #1976d2;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #1976d2;
  gap: 4px;
}
.add-image-btn:hover { background: #e3f2fd; }
.plus-icon { font-size: 1.6rem; line-height: 1; }
.count-txt { font-size: 0.75rem; }
.error-msg { color: #e53935; font-size: 0.84rem; }
</style>
