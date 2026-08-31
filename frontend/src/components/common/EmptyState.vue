<script setup lang="ts">
defineProps<{
  title: string
  description?: string
}>()
</script>

<template>
  <div class="empty-state">
    <svg class="empty-icon" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
      <rect x="3" y="3" width="18" height="18" rx="2"/>
      <line x1="9" y1="9" x2="15" y2="9"/>
      <line x1="9" y1="13" x2="13" y2="13"/>
      <line x1="9" y1="17" x2="15" y2="17"/>
    </svg>
    <h3 class="empty-title">{{ title }}</h3>
    <p v-if="description" class="empty-desc">{{ description }}</p>
  </div>
</template>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 24px;
  text-align: center;
}
.empty-icon {
  color: var(--border);
  margin-bottom: 20px;
}
.empty-title {
  font-size: 17px;
  font-weight: 700;
  color: var(--primary);
  margin-bottom: 8px;
}
.empty-desc {
  font-size: 14px;
  color: var(--text-muted);
}
</style>
