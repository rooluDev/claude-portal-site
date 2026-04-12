<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  existingFiles: { type: Array, default: () => [] },
  maxCount: { type: Number, default: 5 },
  maxSizeMb: { type: Number, default: 20 },
})

const emit = defineEmits(['update:newFiles', 'update:deleteIds'])

const newFiles = ref([])
const pendingDeleteIds = ref([])
const errorMsg = ref('')

const remainingExisting = computed(() =>
  props.existingFiles.filter(f => !pendingDeleteIds.value.includes(f.id))
)

const totalCount = computed(() => remainingExisting.value.length + newFiles.value.length)

function handleFileChange(e) {
  errorMsg.value = ''
  const selected = Array.from(e.target.files)
  for (const file of selected) {
    if (file.size > props.maxSizeMb * 1024 * 1024) {
      errorMsg.value = `파일 크기는 ${props.maxSizeMb}MB를 초과할 수 없습니다.`
      e.target.value = ''
      return
    }
    if (totalCount.value + newFiles.value.indexOf(file) >= props.maxCount) {
      errorMsg.value = `첨부파일은 최대 ${props.maxCount}개까지 업로드할 수 있습니다.`
      e.target.value = ''
      return
    }
  }
  if (totalCount.value + selected.length > props.maxCount) {
    errorMsg.value = `첨부파일은 최대 ${props.maxCount}개까지 업로드할 수 있습니다.`
    e.target.value = ''
    return
  }
  newFiles.value = [...newFiles.value, ...selected]
  emit('update:newFiles', newFiles.value)
  e.target.value = ''
}

function removeNew(idx) {
  newFiles.value.splice(idx, 1)
  emit('update:newFiles', [...newFiles.value])
}

function removeExisting(id) {
  pendingDeleteIds.value.push(id)
  emit('update:deleteIds', [...pendingDeleteIds.value])
}

function restoreExisting(id) {
  pendingDeleteIds.value = pendingDeleteIds.value.filter(i => i !== id)
  emit('update:deleteIds', [...pendingDeleteIds.value])
}
</script>

<template>
  <div class="file-attachment">
    <div class="existing-files" v-if="existingFiles.length">
      <div
        v-for="file in existingFiles"
        :key="file.id"
        :class="['file-item', { deleted: pendingDeleteIds.includes(file.id) }]"
      >
        <span class="file-name">{{ file.originalName }}</span>
        <button
          v-if="!pendingDeleteIds.includes(file.id)"
          class="btn-remove"
          @click="removeExisting(file.id)"
        >×</button>
        <button v-else class="btn-restore" @click="restoreExisting(file.id)">복원</button>
      </div>
    </div>

    <div class="new-files" v-if="newFiles.length">
      <div v-for="(file, idx) in newFiles" :key="idx" class="file-item">
        <span class="file-name new-file">{{ file.name }}</span>
        <button class="btn-remove" @click="removeNew(idx)">×</button>
      </div>
    </div>

    <label class="btn-add-file">
      파일 추가 ({{ totalCount }}/{{ maxCount }})
      <input type="file" multiple hidden @change="handleFileChange" />
    </label>

    <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
  </div>
</template>

<style scoped>
.file-attachment { display: flex; flex-direction: column; gap: 8px; }
.file-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 0.88rem;
}
.file-item.deleted .file-name {
  text-decoration: line-through;
  color: #e53935;
}
.file-name { flex: 1; color: #444; }
.file-name.new-file { color: #1976d2; }
.btn-remove {
  color: #999;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  line-height: 1;
  padding: 0 4px;
}
.btn-remove:hover { color: #e53935; }
.btn-restore {
  font-size: 0.78rem;
  color: #1976d2;
  background: none;
  border: 1px solid #1976d2;
  border-radius: 3px;
  cursor: pointer;
  padding: 2px 6px;
}
.btn-add-file {
  display: inline-flex;
  align-items: center;
  padding: 7px 14px;
  background: #fff;
  border: 1px dashed #1976d2;
  color: #1976d2;
  border-radius: 4px;
  font-size: 0.88rem;
  cursor: pointer;
  width: fit-content;
}
.btn-add-file:hover { background: #e3f2fd; }
.error-msg { color: #e53935; font-size: 0.84rem; }
</style>
