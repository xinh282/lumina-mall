<script setup lang="ts">
import { ref } from 'vue'

const message = ref('')
const visible = ref(false)
let timer: ReturnType<typeof setTimeout> | null = null

function show(msg: string) {
  message.value = msg
  visible.value = true
  if (timer) clearTimeout(timer)
  timer = setTimeout(() => {
    visible.value = false
  }, 2200)
}

defineExpose({ show })
</script>

<template>
  <div class="toast" :class="{ visible }" role="alert">
    {{ message }}
  </div>
</template>

<style scoped>
.toast {
  position: fixed;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%) translateY(20px);
  background: var(--primary);
  color: white;
  padding: 12px 28px;
  border-radius: 28px;
  font-size: 14px;
  font-weight: 500;
  letter-spacing: 0.5px;
  z-index: 9999;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.35s ease, transform 0.35s ease;
  white-space: nowrap;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}
.toast.visible {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
  pointer-events: auto;
}
</style>
