<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCategoriesApi } from '@/api/category'
import type { Category } from '@/types'

const props = defineProps<{ keyword?: string }>()
const emit = defineEmits<{
  'filter-change': [payload: { categoryId?: number; sort?: string; keyword?: string }]
}>()

const categories = ref<Category[]>([])
const selectedCategory = ref<number | undefined>(undefined)
const selectedSort = ref<string>('default')
const searchText = ref(props.keyword || '')

const sortOptions = [
  { label: '综合', value: 'default' },
  { label: '价格升序', value: 'price_asc' },
  { label: '价格降序', value: 'price_desc' },
  { label: '销量优先', value: 'sales_desc' },
  { label: '最新', value: 'newest' },
]

onMounted(async () => {
  try { categories.value = await getCategoriesApi() } catch {
    categories.value = [
      { id: 1, name: '女装', icon: '', sortOrder: 1 },
      { id: 2, name: '男装', icon: '', sortOrder: 2 },
      { id: 3, name: '配饰', icon: '', sortOrder: 3 },
      { id: 4, name: '生活方式', icon: '', sortOrder: 4 },
    ]
  }
})

function emitChange() {
  emit('filter-change', {
    categoryId: selectedCategory.value,
    sort: selectedSort.value,
    keyword: searchText.value || undefined,
  })
}

let searchTimer: ReturnType<typeof setTimeout> | undefined
function onSearchInput() {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => emitChange(), 300)
}

function onCategoryChange(val: number | undefined) { selectedCategory.value = val; emitChange() }
function onSortChange(val: string) { selectedSort.value = val; emitChange() }
</script>

<template>
  <div class="filter-bar">
    <div class="filter-left">
      <span class="filter-label">分类</span>
      <el-select v-model="selectedCategory" placeholder="全部品类" clearable @change="onCategoryChange" class="filter-select">
        <el-option label="全部品类" :value="undefined" />
        <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
      </el-select>
      <el-input
        v-model="searchText"
        placeholder="搜索商品..."
        clearable
        class="search-input"
        @input="onSearchInput"
        @clear="onSearchInput"
      >
        <template #prefix>
          <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2" style="color:var(--text-muted)"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
        </template>
      </el-input>
    </div>
    <div class="filter-right">
      <span class="filter-label">排序</span>
      <el-radio-group v-model="selectedSort" @change="onSortChange">
        <el-radio-button v-for="opt in sortOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</el-radio-button>
      </el-radio-group>
    </div>
  </div>
</template>

<style scoped>
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px 20px;
  background: white;
  border-radius: 12px;
  border: 1px solid var(--border);
  margin-bottom: 24px;
}
.filter-left,
.filter-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.filter-label {
  font-size: 13px;
  color: var(--text-muted);
  white-space: nowrap;
}
.filter-select { width: 140px; }
.search-input { width: 200px; }
@media (max-width: 640px) {
  .filter-bar { flex-direction: column; align-items: flex-start; }
  .search-input { width: 100%; }
}
</style>
