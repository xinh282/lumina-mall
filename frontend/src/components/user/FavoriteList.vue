<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getFavoritesApi, toggleFavoriteApi } from '@/api/favorite'
import type { Product } from '@/types'
import { formatPrice } from '@/utils/format'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const products = ref<Product[]>([])
const loading = ref(false)

onMounted(async () => { loading.value = true; try { products.value = await getFavoritesApi() } catch {} finally { loading.value = false } })

async function removeFav(p: Product) {
  try { await toggleFavoriteApi(p.id); products.value = products.value.filter(x => x.id !== p.id); ElMessage.success('已取消收藏') } catch {}
}
</script>

<template>
  <div class="fav-list" v-loading="loading">
    <div v-if="!products.length && !loading" class="empty">暂无收藏</div>
    <div v-for="p in products" :key="p.id" class="fav-row" @click="router.push('/product/' + p.id)">
      <div class="fav-img"><img v-if="p.image" :src="p.image" :alt="p.name" /><span v-else>📦</span></div>
      <div class="fav-info">
        <div class="fav-name">{{ p.name }}</div>
        <div class="fav-price">{{ formatPrice(p.price) }}</div>
      </div>
      <el-button size="small" type="danger" link @click.stop="removeFav(p)">取消收藏</el-button>
    </div>
  </div>
</template>

<style scoped>
.fav-row { display: flex; align-items: center; gap: 14px; padding: 12px; border-radius: 8px; cursor: pointer; transition: background .2s; }
.fav-row:hover { background: var(--bg-warm); }
.fav-img { width: 56px; height: 56px; background: #f5f3ef; border-radius: 8px; overflow: hidden; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.fav-img img { width: 100%; height: 100%; object-fit: cover; }
.fav-info { flex: 1; }
.fav-name { font-size: 14px; font-weight: 600; }
.fav-price { font-size: 14px; font-weight: 700; color: var(--accent); margin-top: 2px; }
.empty { text-align: center; padding: 48px; color: var(--text-muted); }
</style>
