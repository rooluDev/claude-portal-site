<script setup>
defineProps({ message: { type: String, required: true } })
const emit = defineEmits(['confirm', 'cancel'])
</script>

<template>
  <div class="dialog-backdrop" @click.self="emit('cancel')">
    <div class="dialog-box">
      <p class="dialog-msg">{{ message }}</p>
      <div class="dialog-actions">
        <button class="btn-cancel" @click="emit('cancel')">취소</button>
        <button class="btn-confirm" @click="emit('confirm')">확인</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dialog-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1100;
}
.dialog-box {
  background: #fff;
  border-radius: 8px;
  padding: 28px 24px;
  min-width: 300px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}
.dialog-msg {
  font-size: 0.97rem;
  color: #333;
  margin-bottom: 24px;
  line-height: 1.6;
}
.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.btn-cancel {
  padding: 8px 18px;
  border: 1px solid #bbb;
  background: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}
.btn-cancel:hover { background: #f5f5f5; }
.btn-confirm {
  padding: 8px 18px;
  border: none;
  background: #e53935;
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}
.btn-confirm:hover { background: #c62828; }
</style>
