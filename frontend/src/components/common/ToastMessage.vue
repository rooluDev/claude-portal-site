<script setup>
import { useToastStore } from '@/stores/toast'

const toastStore = useToastStore()
</script>

<template>
  <div class="toast-container">
    <TransitionGroup name="toast">
      <div
        v-for="toast in toastStore.toasts"
        :key="toast.id"
        :class="['toast-item', `toast-${toast.type}`]"
      >
        {{ toast.message }}
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.toast-container {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 2000;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.toast-item {
  padding: 12px 20px;
  border-radius: 6px;
  color: #fff;
  font-size: 0.9rem;
  min-width: 220px;
  max-width: 360px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}
.toast-success { background: #388e3c; }
.toast-error   { background: #d32f2f; }
.toast-info    { background: #1976d2; }

.toast-enter-active, .toast-leave-active { transition: all 0.3s ease; }
.toast-enter-from { opacity: 0; transform: translateY(12px); }
.toast-leave-to   { opacity: 0; transform: translateY(12px); }
</style>
