<template>
  <div class="home">
    <HeroBanner />

    <FeaturesBar />

    <!-- 限时秒杀 -->
    <section class="section seckill-section-home" v-if="seckillProducts.length">
      <div class="section-header">
        <h2 class="section-title">限时秒杀</h2>
        <p class="section-subtitle">手慢无，限时特惠</p>
      </div>
      <div class="seckill-grid">
        <div v-for="p in seckillProducts" :key="p.id" class="seckill-item" @click="goToProduct(p.id)">
          <div class="seckill-item-img">
            <img v-if="p.image" :src="p.image" :alt="p.name" />
            <span v-else>商品图</span>
          </div>
          <div class="seckill-item-info">
            <p class="seckill-item-name">{{ p.name }}</p>
            <div class="seckill-item-prices">
              <span class="seckill-item-price">¥{{ p.seckillPrice }}</span>
              <span class="seckill-item-original">¥{{ p.price }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 本周精选 -->
    <section class="section" v-if="!hotLoading && hotProducts.length">
      <div class="section-header">
        <h2 class="section-title">本周精选</h2>
        <p class="section-subtitle">编辑团队为你精心挑选</p>
      </div>
      <div class="carousel-wrap">
        <button class="carousel-arrow left" @click="scrollHot(-1)" :disabled="hotScroll === 0" aria-label="上一个">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><polyline points="15 18 9 12 15 6"/></svg>
        </button>
        <div class="carousel-track" ref="hotTrackRef" @scroll="onHotScroll">
          <div class="carousel-slide" v-for="p in hotProducts" :key="p.id">
            <ProductCard :product="p" @add-to-cart="handleAddToCart" />
          </div>
        </div>
        <button class="carousel-arrow right" @click="scrollHot(1)" :disabled="hotScroll >= hotMaxScroll" aria-label="下一个">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 6 15 12 9 18"/></svg>
        </button>
      </div>
      <div class="carousel-dots">
        <span v-for="d in hotDots" :key="d" class="dot" :class="{ active: d === hotActiveDot }" @click="scrollToDot(d)" />
      </div>
    </section>

    <section class="section" v-if="hotLoading">
      <div class="section-header">
        <h2 class="section-title">本周精选</h2>
        <p class="section-subtitle">编辑团队为你精心挑选</p>
      </div>
      <div class="loading-placeholder">
        <el-skeleton :rows="3" animated />
      </div>
    </section>

    <section class="section empty-section" v-if="!hotLoading && !hotProducts.length">
      <div class="section-header">
        <h2>本周精选</h2>
        <p>编辑团队为你精心挑选</p>
      </div>
      <div class="empty-hint">管理后台将商品标记为「热卖」即可在这里展示</div>
    </section>

    <!-- 新品上市 -->
    <section class="section" v-if="!newLoading && newProducts.length">
      <div class="section-header">
        <h2 class="section-title">新品上市</h2>
        <p class="section-subtitle">抢先体验最新单品</p>
      </div>
      <ProductGrid :products="newProducts" @add-to-cart="handleAddToCart" />
    </section>

    <section class="section" v-if="newLoading">
      <div class="section-header">
        <h2 class="section-title">新品上市</h2>
        <p class="section-subtitle">抢先体验最新单品</p>
      </div>
      <div class="loading-placeholder">
        <el-skeleton :rows="3" animated />
      </div>
    </section>

    <section class="section empty-section" v-if="!newLoading && !newProducts.length">
      <div class="section-header">
        <h2 class="section-title">新品上市</h2>
        <p class="section-subtitle">抢先体验最新单品</p>
      </div>
      <div class="empty-hint">管理后台将商品标记为「新品」即可在这里展示</div>
    </section>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'
import HeroBanner from '@/components/home/HeroBanner.vue'
import FeaturesBar from '@/components/home/FeaturesBar.vue'

import ProductGrid from '@/components/home/ProductGrid.vue'
import ProductCard from '@/components/product/ProductCard.vue'
import { getHotProductsApi, getNewProductsApi } from '@/api/product'
import { getSeckillProductsApi } from '@/api/seckill'
import type { Product } from '@/types'

const cartStore = useCartStore()
const router = useRouter()

const hotProducts = ref<Product[]>([])
const newProducts = ref<Product[]>([])
const seckillProducts = ref<Product[]>([])

// 横向滚动轮播
const hotTrackRef = ref<HTMLElement | null>(null)
const hotScroll = ref(0)
const hotMaxScroll = ref(0)
const hotDots = ref(0)
const hotActiveDot = ref(0)
let hotTimer: ReturnType<typeof setInterval> | null = null

function updateHotScroll() {
  const el = hotTrackRef.value
  if (!el) return
  const cardW = el.firstElementChild?.clientWidth ?? 220
  const gap = 24
  const visibleW = el.clientWidth
  const totalW = el.scrollWidth
  hotMaxScroll.value = Math.max(0, totalW - visibleW)
  hotDots.value = Math.ceil(el.children.length / Math.floor(visibleW / (cardW + gap)))
}

function onHotScroll() {
  const el = hotTrackRef.value
  if (!el) return
  hotScroll.value = el.scrollLeft
  const visibleW = el.clientWidth
  hotActiveDot.value = Math.round(el.scrollLeft / (visibleW * 0.8))
}

function scrollHot(dir: number) {
  const el = hotTrackRef.value
  if (!el) return
  const visibleW = el.clientWidth
  el.scrollBy({ left: dir * visibleW * 0.75, behavior: 'smooth' })
}

function scrollToDot(i: number) {
  const el = hotTrackRef.value
  if (!el) return
  const visibleW = el.clientWidth
  el.scrollTo({ left: i * visibleW * 0.8, behavior: 'smooth' })
}

function startHotAutoScroll() {
  stopHotAutoScroll()
  hotTimer = setInterval(() => {
    const el = hotTrackRef.value
    if (!el) return
    const visibleW = el.clientWidth
    if (el.scrollLeft + visibleW >= el.scrollWidth - 10) {
      el.scrollTo({ left: 0, behavior: 'smooth' })
    } else {
      el.scrollBy({ left: visibleW * 0.75, behavior: 'smooth' })
    }
  }, 3000)
}

function stopHotAutoScroll() {
  if (hotTimer) { clearInterval(hotTimer); hotTimer = null }
}
const hotLoading = ref(true)
const newLoading = ref(true)

function goToProduct(id: number) {
  router.push('/product/' + id)
}

onMounted(async () => {
  try {
    seckillProducts.value = await getSeckillProductsApi()
  } catch { /* ignore */ }

  try {
    const res = await getHotProductsApi(8)
    hotProducts.value = (res as any).data ?? res ?? []
    await nextTick()
    updateHotScroll()
    if (hotProducts.value.length) startHotAutoScroll()
  } catch {
    // silently handle error
  } finally {
    hotLoading.value = false
  }

  try {
    const res = await getNewProductsApi(8)
    newProducts.value = (res as any).data ?? res ?? []
  } catch {
    // silently handle error
  } finally {
    newLoading.value = false
  }
})

onUnmounted(() => {
  stopHotAutoScroll()
})

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
.home {
  min-height: 100vh;
}

.section {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 52px 32px;
}
.section:first-of-type { padding-top: 40px; }

.section-header {
  text-align: center;
  margin-bottom: 36px;
}

.section-header h2 {
  font-family: var(--font-display);
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 1px;
  color: var(--text);
  margin: 0 0 8px;
}

.section-header p {
  color: var(--text-muted);
  font-size: 14px;
  margin: 0;
}

.carousel-wrap {
  display: flex;
  align-items: center;
  position: relative;
}
.carousel-track {
  display: flex;
  gap: 24px;
  overflow-x: auto;
  scroll-behavior: smooth;
  scroll-snap-type: x mandatory;
  -webkit-overflow-scrolling: touch;
  padding: 8px 0;
  scrollbar-width: none;
}
.carousel-track::-webkit-scrollbar { display: none; }
.carousel-slide {
  flex: 0 0 calc(25% - 18px);
  min-width: 220px;
  scroll-snap-align: start;
}
.carousel-arrow {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 1px solid var(--border);
  background: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text);
  transition: all 0.2s ease;
  z-index: 2;
}
.carousel-arrow:hover:not(:disabled) {
  background: var(--primary);
  color: white;
  border-color: var(--primary);
}
.carousel-arrow:disabled {
  opacity: 0.3;
  cursor: default;
}
.carousel-arrow.left { margin-right: 8px; }
.carousel-arrow.right { margin-left: 8px; }
.carousel-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 20px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--border);
  cursor: pointer;
  transition: all 0.3s ease;
}
.dot.active {
  background: var(--accent);
  width: 24px;
  border-radius: 4px;
}
/* Seckill */
.seckill-section-home {
  background: linear-gradient(180deg, #fef9f0 0%, var(--bg) 100%);
  padding-top: 48px;
  padding-bottom: 16px;
}
.seckill-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}
.seckill-item {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid var(--border);
  transition: transform 0.2s, box-shadow 0.2s;
}
.seckill-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
}
.seckill-item-img {
  width: 100%;
  aspect-ratio: 1;
  background: #f5f3ef;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.seckill-item-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.seckill-item-info {
  padding: 12px 14px;
}
.seckill-item-name {
  font-size: 14px;
  color: var(--text);
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.seckill-item-prices {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.seckill-item-price {
  font-size: 18px;
  font-weight: 700;
  color: var(--accent);
}
.seckill-item-original {
  font-size: 12px;
  color: var(--text-muted);
  text-decoration: line-through;
}

.empty-section {
  padding-top: 60px;
  padding-bottom: 60px;
}
.empty-hint {
  text-align: center;
  color: var(--text-muted);
  font-size: 14px;
  margin-top: 12px;
}
.loading-placeholder {
  max-width: 600px;
  margin: 0 auto;
}

@media (max-width: 1024px) {
  .carousel-slide { flex: 0 0 calc(33.33% - 16px); }
  .carousel-arrow { display: none; }
}
@media (max-width: 640px) {
  .carousel-slide { flex: 0 0 calc(50% - 12px); min-width: 160px; }
  .carousel-track { gap: 12px; }
  .carousel-arrow { display: none; }
}

@media (max-width: 768px) {
  .section {
    padding: 48px 16px;
  }

  .section-header {
    margin-bottom: 32px;
  }

  .section-header h2 {
    font-size: 22px;
  }
}
</style>
