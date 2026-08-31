<template>
  <div class="category-page">
    <ProductFilter
      :keyword="keyword"
      @filter-change="handleFilterChange"
    />

    <div class="product-area" v-loading="loading">
      <ProductGrid
        v-if="products.length"
        :products="products"
        @add-to-cart="handleAddToCart"
      />
      <EmptyState v-else-if="!loading" title="暂无相关商品" />

      <Pagination
        v-if="total > 0"
        :page="page"
        :total="total"
        :size="size"
        @page-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'
import ProductFilter from '@/components/product/ProductFilter.vue'
import ProductGrid from '@/components/home/ProductGrid.vue'
import Pagination from '@/components/product/Pagination.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { getProductListApi } from '@/api/product'
import type { Product } from '@/types'

const route = useRoute()
const cartStore = useCartStore()

const products = ref<Product[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(12)
const categoryId = ref<number | undefined>(undefined)
const sort = ref('')
const keyword = ref('')
const loading = ref(false)

watch(
  () => [route.params.id, route.query.keyword],
  ([newId, newKeyword]) => {
    categoryId.value = newId ? Number(newId) : undefined
    keyword.value = (newKeyword as string) || ''
    page.value = 1
    fetchProducts()
  }
)

onMounted(() => {
  if (route.params.id) categoryId.value = Number(route.params.id)
  keyword.value = (route.query.keyword as string) || ''
  fetchProducts()
})

async function fetchProducts() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: page.value, size: size.value }
    if (categoryId.value) params.categoryId = categoryId.value
    if (keyword.value) params.keyword = keyword.value
    if (sort.value) params.sort = sort.value
    const res = await getProductListApi(params)
    products.value = res.records
    total.value = res.total
  } catch {
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleFilterChange(filters: { categoryId?: number; sort?: string; keyword?: string }) {
  if (filters.categoryId !== undefined) categoryId.value = filters.categoryId
  if (filters.sort !== undefined) sort.value = filters.sort
  if (filters.keyword !== undefined) keyword.value = filters.keyword
  page.value = 1
  fetchProducts()
}

function handlePageChange(newPage: number) {
  page.value = newPage
  fetchProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

async function handleAddToCart(productId: number) {
  try {
    await cartStore.addToCart(productId, 1)
    ElMessage.success('已加入购物车')
  } catch {
    ElMessage.error('加入购物车失败')
  }
}
</script>

<style scoped>
.category-page { max-width: var(--max-width); margin: 0 auto; padding: 48px 24px; }
.product-area { min-height: 400px; }
@media (max-width: 768px) { .category-page { padding: 32px 16px; } }
</style>
