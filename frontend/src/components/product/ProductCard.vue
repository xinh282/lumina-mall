<script setup lang="ts">
import { ref } from 'vue'
import type { Product } from '@/types'
import { formatPrice } from '@/utils/format'
import { useRouter } from 'vue-router'
import { toggleFavoriteApi, checkFavoriteApi } from '@/api/favorite'

const props = defineProps<{ product: Product }>()
const emit = defineEmits<{ 'add-to-cart': [id: number] }>()
const router = useRouter()
const favorited = ref(false)

checkFavoriteApi(props.product.id).then(r => { favorited.value = r as any ?? false }).catch(() => {})

async function toggleFav(e: Event) {
  e.stopPropagation()
  try { const r = await toggleFavoriteApi(props.product.id); favorited.value = r as any ?? false } catch {}
}

const bgPalette = [
  '#f5f2ed', '#edf0f4', '#f2edf5', '#edf5f0',
  '#f5efed', '#f0f2f0', '#f5f0ed', '#eff0f5',
]

function bgColor() { return bgPalette[(props.product.id - 1) % bgPalette.length] }

function goDetail() { router.push('/product/' + props.product.id) }
function onQuickAdd(e: Event) { e.stopPropagation(); emit('add-to-cart', props.product.id) }
</script>

<template>
  <div class="product-card" @click="goDetail">
    <div class="card-img-wrap" :style="{ background: bgColor() }">
      <img v-if="product.image" :src="product.image" :alt="product.name" loading="lazy" class="card-img" />
      <span v-else class="no-img">{{ product.name?.charAt(0) }}</span>
      <span v-if="product.badge" class="tag" :class="'tag-' + (product.badge || 'new')">{{ product.badgeText || product.badge }}</span>
      <button class="fav-btn" :class="{ active: favorited }" @click="toggleFav" :title="favorited ? '取消收藏' : '收藏'">
        <svg width="16" height="16" viewBox="0 0 24 24" :fill="favorited ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
      </button>
      <button class="quick-add" @click="onQuickAdd" title="加购">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
      </button>
    </div>
    <div class="card-body">
      <p class="card-name">{{ product.name }}</p>
      <div class="card-prices">
        <span class="card-price">{{ formatPrice(product.price) }}</span>
        <span v-if="product.originalPrice" class="card-original">{{ formatPrice(product.originalPrice) }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-card {
  cursor: pointer;
  transition: transform var(--transition);
}
.product-card:hover { transform: translateY(-4px); }

/* Image */
.card-img-wrap {
  position: relative;
  aspect-ratio: 1;
  border-radius: var(--radius-sm);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
}
.card-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.product-card:hover .card-img { transform: scale(1.04); }

.fav-btn {
  position: absolute; top: 8px; right: 10px; width: 30px; height: 30px;
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  color: #999; transition: all .2s; opacity: 0;
}
.product-card:hover .fav-btn { opacity: 1; }
.fav-btn:hover { color: #e74c3c; }
.fav-btn.active { opacity: 1; color: #e74c3c; }

.no-img {
  font-family: var(--font-display);
  font-size: 48px;
  color: rgba(0,0,0,0.1);
}

/* Tag */
.tag {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  color: #fff;
}
.tag-new { background: #1a1a1a; }
.tag-sale { background: #d9534f; }
.tag-hot { background: var(--accent); color: #1a1a1a; }

/* Quick add */
.quick-add {
  position: absolute;
  bottom: 8px;
  right: 8px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #fff;
  color: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: translateY(6px);
  transition: all var(--transition);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.product-card:hover .quick-add { opacity: 1; transform: translateY(0); }
.quick-add:hover { background: #1a1a1a; color: #fff; }

/* Body */
.card-body { padding: 0 2px; }
.card-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 4px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.card-prices { display: flex; align-items: baseline; gap: 6px; }
.card-price { font-size: 14px; font-weight: 700; color: #1a1a1a; }
.card-original { font-size: 12px; color: var(--text-muted); text-decoration: line-through; }
</style>
