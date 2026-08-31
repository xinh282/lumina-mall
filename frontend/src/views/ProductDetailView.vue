<template>
  <div class="product-detail" v-loading="loading">
    <template v-if="product">
      <!-- Breadcrumb -->
      <div class="breadcrumb" v-if="product.categoryName">
        <router-link to="/">首页</router-link>
        <span class="separator">/</span>
        <router-link :to="`/category/${product.categoryId}`">
          {{ product.categoryName }}
        </router-link>
        <span class="separator">/</span>
        <span class="current">{{ product.name }}</span>
      </div>

      <div class="detail-layout">
        <!-- Left: Image -->
        <div class="image-area">
          <div class="image-placeholder">
            <img
              v-if="product.image"
              :src="product.image"
              :alt="product.name"
            />
            <span v-else class="name-initial">{{ product.name.charAt(0) }}</span>
          </div>
        </div>

        <!-- Right: Info -->
        <div class="info-area">
          <div class="name-row">
            <h1 class="product-name">{{ product.name }}</h1>
            <button class="fav-btn-detail" :class="{ active: favorited }" @click="toggleFav" :title="favorited ? '取消收藏' : '收藏'">
              <svg width="22" height="22" viewBox="0 0 24 24" :fill="favorited ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
            </button>
          </div>

          <div class="price-area">
            <span class="current-price">{{ formatPrice(product.price) }}</span>
            <span
              v-if="product.originalPrice && product.originalPrice > product.price"
              class="original-price"
            >
              {{ formatPrice(product.originalPrice) }}
            </span>
          </div>

          <el-tag
            v-if="product.badge"
            size="small"
            class="badge-tag"
            effect="dark"
          >
            {{ product.badgeText || product.badge }}
          </el-tag>

          <p class="description" v-if="product.description">
            {{ product.description }}
          </p>

          <div class="stock-info">
            <span v-if="product.stock > 0" class="in-stock">有货</span>
            <span v-else class="out-of-stock">暂时缺货</span>
            <span class="stock-count" v-if="product.stock > 0 && product.stock <= 20">
              仅剩 {{ product.stock }} 件
            </span>
          </div>

          <!-- SKU 规格选择 -->
          <div v-if="specTypes.length" class="spec-section">
            <div v-for="type in specTypes" :key="type" class="spec-group">
              <span class="spec-label">{{ type }}</span>
              <div class="spec-options">
                <button
                  v-for="opt in (specOptions.get(type) || [])" :key="opt"
                  class="spec-btn"
                  :class="{ active: selectedSpecs.get(type) === opt }"
                  @click="selectSpec(type, opt)"
                >{{ opt }}</button>
              </div>
            </div>
          </div>

          <!-- Quantity Selector -->
          <div class="quantity-row" v-if="product.stock > 0">
            <span class="qty-label">数量</span>
            <div class="quantity-selector">
              <button
                class="qty-btn"
                :disabled="quantity <= 1"
                @click="quantity = Math.max(1, quantity - 1)"
              >
                -
              </button>
              <span class="qty-value">{{ quantity }}</span>
              <button
                class="qty-btn"
                :disabled="quantity >= product.stock"
                @click="quantity = Math.min(product.stock, quantity + 1)"
              >
                +
              </button>
            </div>
          </div>

          <!-- Add to Cart -->
          <el-button
            v-if="product.stock > 0"
            type="primary"
            size="large"
            class="add-to-cart-btn"
            :loading="addingToCart"
            @click="handleAddToCart"
          >
            加入购物车
          </el-button>

          <!-- Seckill Section -->
          <div v-if="product.seckillPrice" class="seckill-section">
            <div class="seckill-header">
              <span class="seckill-label">限时秒杀</span>
            </div>
            <div class="seckill-price-row">
              <span class="seckill-price">
                秒杀价 {{ formatPrice(product.seckillPrice) }}
              </span>
              <span class="seckill-original">{{ formatPrice(product.price) }}</span>
            </div>
            <div v-if="seckillActive" class="countdown">
              距结束：{{ formatCountdown(seckillRemaining) }}
            </div>
            <div v-else-if="seckillRemaining > 0" class="countdown upcoming">
              距开始：{{ formatCountdown(seckillRemaining) }}
            </div>
            <div v-else class="countdown expired">
              活动已结束
            </div>
            <el-button
              type="warning"
              size="large"
              class="seckill-btn"
              :loading="seckilling"
              :disabled="!seckillActive"
              @click="handleSeckill"
            >
              {{ seckillActive ? '立即抢购' : seckillRemaining > 0 ? '尚未开始' : '已结束' }}
            </el-button>
          </div>
        </div>
      </div>
    </template>

    <!-- 商品评价 -->
    <ReviewSection v-if="product" :product-id="product.id" />

    <EmptyState v-else-if="!loading" title="商品不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'
import { formatPrice } from '@/utils/format'
import EmptyState from '@/components/common/EmptyState.vue'
import ReviewSection from '@/components/product/ReviewSection.vue'
import { getProductDetailApi } from '@/api/product'
import { seckillApi } from '@/api/seckill'
import { getSkusApi } from '@/api/sku'
import { toggleFavoriteApi, checkFavoriteApi } from '@/api/favorite'
import type { ProductSku } from '@/api/sku'
import type { Product } from '@/types'

const route = useRoute()
const cartStore = useCartStore()

const product = ref<Product | null>(null)
const loading = ref(true)
const quantity = ref(1)
const addingToCart = ref(false)
const seckilling = ref(false)
const seckillRemaining = ref(0)
const seckillActive = ref(false)
let countdownTimer: ReturnType<typeof setInterval> | null = null

// SKU
const skus = ref<ProductSku[]>([])
const specTypes = ref<string[]>([])  // ['颜色', '尺码']
const specOptions = ref<Map<string, string[]>>(new Map())  // {'颜色':['黑','白'], '尺码':['S','M','L']}
const selectedSpecs = ref<Map<string, string>>(new Map())
const selectedSkuId = ref<number | null>(null)
const favorited = ref(false)

watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      loadProduct(newId as string)
    }
  }
)

onMounted(() => {
  loadProduct(route.params.id as string)
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})

async function loadProduct(id: string) {
  loading.value = true
  quantity.value = 1

  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }

  try {
    const res = await getProductDetailApi(Number(id))
    product.value = (res as any).data ?? res

    if (product.value?.seckillPrice) {
      updateCountdown()
      countdownTimer = setInterval(updateCountdown, 1000)
    }
  } catch {
    product.value = null
  } finally {
    loading.value = false
  }

  // 加载 SKU
  try {
    skus.value = await getSkusApi(Number(id))
    parseSpecs()
  } catch { skus.value = [] }
  try { const r = await checkFavoriteApi(Number(id)); favorited.value = r as any ?? false } catch {}
}

function parseSpecs() {
  if (!skus.value.length) return
  // 从第一个 SKU 的 specs 推断规格名，如 "黑色;M" → 可能代表 "颜色;尺码"
  const types = ['颜色', '尺码']
  specTypes.value = types.slice(0, skus.value[0].specs.split(';').length)
  const map = new Map<string, Set<string>>()
  specTypes.value.forEach(t => map.set(t, new Set()))
  skus.value.forEach(s => {
    const parts = s.specs.split(';')
    parts.forEach((p, i) => {
      if (specTypes.value[i]) map.get(specTypes.value[i])?.add(p.trim())
    })
  })
  const result = new Map<string, string[]>()
  map.forEach((v, k) => result.set(k, [...v]))
  specOptions.value = result
}

async function toggleFav() {
  if (!product.value) return
  try { const r = await toggleFavoriteApi(product.value.id); favorited.value = r as any ?? false } catch {}
}

function selectSpec(type: string, value: string) {
  const m = new Map(selectedSpecs.value)
  if (m.get(type) === value) { m.delete(type) } else { m.set(type, value) }
  selectedSpecs.value = m
  // 匹配SKU
  const selected = skus.value.find(s => {
    const parts = s.specs.split(';')
    return specTypes.value.every((t, i) => {
      const sel = m.get(t)
      return !sel || parts[i]?.trim() === sel
    })
  })
  selectedSkuId.value = selected?.id ?? null
}

function updateCountdown() {
  if (!product.value?.seckillPrice) {
    seckillRemaining.value = 0
    return
  }
  const now = Date.now()
  const start = product.value.seckillStart ? new Date(product.value.seckillStart).getTime() : 0
  const end = product.value.seckillEnd ? new Date(product.value.seckillEnd).getTime() : 0

  if (start && now < start) {
    // 未开始，倒计时到开始时间
    seckillActive.value = false
    seckillRemaining.value = Math.max(0, Math.floor((start - now) / 1000))
  } else if (end && now < end) {
    // 进行中，倒计时到结束时间
    seckillActive.value = true
    seckillRemaining.value = Math.max(0, Math.floor((end - now) / 1000))
  } else {
    // 已结束
    seckillActive.value = false
    seckillRemaining.value = 0
    if (countdownTimer) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }
}

function formatCountdown(seconds: number): string {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
}

async function handleAddToCart() {
  if (!product.value) return
  addingToCart.value = true
  try {
    await cartStore.addToCart(product.value.id, quantity.value, selectedSkuId.value ?? undefined)
    ElMessage.success('已加入购物车')
    quantity.value = 1
  } catch {
    ElMessage.error('加入购物车失败')
  } finally {
    addingToCart.value = false
  }
}

async function handleSeckill() {
  if (!product.value) return
  seckilling.value = true
  try {
    await seckillApi(product.value.id)
    ElMessage.success('秒杀成功！')
  } catch {
    ElMessage.error('秒杀失败，请重试')
  } finally {
    seckilling.value = false
  }
}
</script>

<style scoped>
.product-detail {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 48px 24px;
  min-height: 400px;
}

.breadcrumb {
  font-size: 13px;
  color: var(--text-muted);
  margin-bottom: 32px;
}

.breadcrumb a {
  color: var(--text-muted);
  text-decoration: none;
  transition: color var(--transition);
}

.breadcrumb a:hover {
  color: var(--accent);
}

.separator {
  margin: 0 8px;
}

.current {
  color: var(--text);
}

.detail-layout {
  display: flex;
  gap: 72px;
  margin-top: 21px;
}

.image-area {
  flex: 0 0 50%;
  position: sticky;
  top: calc(var(--header-height) + 24px);
  align-self: flex-start;
}

.image-placeholder {
  aspect-ratio: 4 / 5;
  background: linear-gradient(135deg, #f5f3ef, #e8e4df);
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  font-size: 16px;
  overflow: hidden;
}

.image-placeholder img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.name-initial {
  font-size: 72px;
  font-weight: 700;
  color: var(--accent);
  font-family: var(--font-serif, serif);
  text-transform: uppercase;
}

.info-area {
  flex: 0 0 calc(50% - 64px);
  min-width: 0;
}

.name-row { display: flex; align-items: flex-start; gap: 12px; margin-bottom: 28px; }
.product-name {
  font-family: var(--font-display);
  font-size: 32px; font-weight: 700; color: var(--text);
  margin: 0; line-height: 1.3; flex: 1;
}
.fav-btn-detail {
  flex-shrink: 0; width: 40px; height: 40px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: #ccc; transition: all .2s; margin-top: 2px;
}
.fav-btn-detail:hover { color: #e74c3c; background: rgba(231,76,60,0.08); }
.fav-btn-detail.active { color: #e74c3c; }

.price-area {
  margin-bottom: 24px;
  display: flex;
  align-items: baseline;
  gap: 14px;
}

.current-price {
  font-size: 28px;
  font-weight: 700;
  color: var(--accent);
  font-family: var(--font-display);
}

.original-price {
  font-size: 16px;
  color: var(--text-muted);
  text-decoration: line-through;
}

.badge-tag {
  margin-bottom: 24px;
}

.description {
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.9;
  margin: 0 0 28px;
}

.stock-info {
  margin-bottom: 24px;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.in-stock {
  color: #4caf50;
  font-weight: 600;
}

.out-of-stock {
  color: #f44336;
  font-weight: 600;
}

.stock-count {
  color: #ff9800;
}

/* SKU Specs */
.spec-section { margin-bottom: 20px; }
.spec-group { margin-bottom: 10px; }
.spec-label { font-size: 13px; color: var(--text-secondary); margin-right: 8px; display: inline-block; min-width: 32px; }
.spec-options { display: inline-flex; gap: 8px; flex-wrap: wrap; }
.spec-btn {
  padding: 6px 18px; border: 1px solid var(--border); border-radius: 4px;
  font-size: 13px; background: #fff; cursor: pointer; transition: all .2s;
}
.spec-btn:hover { border-color: var(--text); }
.spec-btn.active { border-color: var(--text); background: var(--text); color: #fff; }

.quantity-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 28px;
}

.qty-label {
  font-size: 14px;
  color: var(--text);
}

.quantity-selector {
  display: inline-flex;
  align-items: center;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm, 6px);
  overflow: hidden;
}

.qty-btn {
  width: 38px;
  height: 38px;
  border: none;
  background: var(--bg);
  color: var(--text);
  font-size: 16px;
  cursor: pointer;
  transition: background var(--transition);
}

.qty-btn:hover:not(:disabled) {
  background: #e8e4df;
}

.qty-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.qty-value {
  width: 48px;
  text-align: center;
  font-size: 15px;
  color: var(--text);
  border-left: 1px solid var(--border);
  border-right: 1px solid var(--border);
  line-height: 38px;
}

.add-to-cart-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  background-color: var(--primary);
  border-color: var(--primary);
  margin-bottom: 24px;
  border-radius: var(--radius, 10px);
  letter-spacing: 1px;
}

.add-to-cart-btn:hover {
  opacity: 0.9;
}

/* Seckill Section */
.seckill-section {
  background: linear-gradient(135deg, #fdf8f0, #fef5e7);
  border: 1px solid var(--accent);
  border-radius: var(--radius, 10px);
  padding: 24px;
}

.seckill-header {
  margin-bottom: 12px;
}

.seckill-label {
  font-size: 14px;
  font-weight: 700;
  color: var(--accent);
  text-transform: uppercase;
  letter-spacing: 2px;
}

.seckill-price-row {
  margin-bottom: 12px;
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.seckill-price {
  font-size: 28px;
  font-weight: 700;
  color: var(--accent);
  font-family: var(--font-serif, serif);
}

.seckill-original {
  font-size: 14px;
  color: var(--text-muted);
  text-decoration: line-through;
}

.countdown {
  font-size: 14px;
  color: var(--text);
  margin-bottom: 16px;
  font-variant-numeric: tabular-nums;
}

.countdown.expired {
  color: var(--text-muted);
}

.countdown.upcoming {
  color: #e67e22;
}

.seckill-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  background-color: var(--accent);
  border-color: var(--accent);
  color: #fff;
  border-radius: var(--radius, 10px);
  letter-spacing: 1px;
}

.seckill-btn:hover {
  opacity: 0.85;
}

/* Responsive */
@media (max-width: 768px) {
  .product-detail {
    padding: 32px 16px;
  }

  .detail-layout {
    flex-direction: column;
    gap: 40px;
  }

  .image-area {
    flex: none;
  }

  .info-area {
    flex: none;
  }

  .product-name {
    font-size: 22px;
  }
}
</style>
