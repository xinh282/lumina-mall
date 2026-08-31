<script setup lang="ts">
defineProps<{
  total: number
  page: number
  size: number
}>()

const emit = defineEmits<{
  'page-change': [page: number]
  'size-change': [size: number]
}>()

function onPageChange(p: number) {
  emit('page-change', p)
}

function onSizeChange(s: number) {
  emit('size-change', s)
}
</script>

<template>
  <div class="pagination-wrapper">
    <el-pagination
      :current-page="page"
      :page-size="size"
      :total="total"
      :page-sizes="[8, 16, 24, 36]"
      layout="total, sizes, prev, pager, next, jumper"
      background
      @current-change="onPageChange"
      @size-change="onSizeChange"
    />
  </div>
</template>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 32px 0;
  background: white;
  border-radius: 12px;
  border: 1px solid var(--border);
  margin-top: 24px;
}
</style>
