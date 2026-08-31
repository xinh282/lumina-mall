<script setup lang="ts">
import { watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { formatPrice } from '@/utils/format'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [v: boolean]; checkout: [] }>()
const cartStore = useCartStore()
const router = useRouter()

watch(() => props.modelValue, (v) => {
  document.body.style.overflow = v ? 'hidden' : ''
})

function close() { emit('update:modelValue', false) }
function handleCheckout() { close(); router.push('/cart') }
function onKeydown(e: KeyboardEvent) { if (e.key === 'Escape') close() }

function increaseQty(item: { id: number; quantity: number }) {
  cartStore.updateQuantity(item.id, item.quantity + 1)
}

function decreaseQty(item: { id: number; quantity: number }) {
  if (item.quantity > 1) {
    cartStore.updateQuantity(item.id, item.quantity - 1)
  }
}

function removeItem(id: number) {
  cartStore.removeItem(id)
}

onMounted(() => document.addEventListener('keydown', onKeydown))
onUnmounted(() => document.removeEventListener('keydown', onKeydown))

const imageGradients = [
  'linear-gradient(135deg, #e8d5c4, #d4a88c)',
  'linear-gradient(135deg, #c4d5e8, #8ca8d4)',
  'linear-gradient(135deg, #d5c4e8, #a88cd4)',
  'linear-gradient(135deg, #c8d5c4, #a0c4a0)',
]

function imageBg(id: number) {
  return imageGradients[id % imageGradients.length]
}
</script>

<template>
  <Teleport to="body">
    <div class="cart-overlay" :class="{ open: modelValue }" @click="close"></div>
    <aside class="cart-sidebar" :class="{ open: modelValue }">
      <div class="cart-header">
        <h2>购物车</h2>
        <button class="cart-close" @click="close" aria-label="关闭">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>

      <div class="cart-body" v-if="cartStore.items.length > 0">
        <div class="cart-item" v-for="item in cartStore.items" :key="item.id">
          <div class="cart-item-image" :style="{ background: imageBg(item.productId) }"></div>
          <div class="cart-item-detail">
            <div class="cart-item-name">{{ item.productName }}</div>
            <div class="cart-item-price">{{ formatPrice(item.productPrice) }}</div>
            <div class="cart-item-qty">
              <button class="qty-btn" @click="decreaseQty(item)">-</button>
              <span class="qty-value">{{ item.quantity }}</span>
              <button class="qty-btn" @click="increaseQty(item)">+</button>
            </div>
          </div>
          <button class="cart-item-remove" @click="removeItem(item.id)" aria-label="删除">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/>
              <line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
      </div>

      <div v-else class="cart-empty">
        <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" opacity="0.3">
          <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
          <path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6"/>
        </svg>
        <p class="cart-empty-title">购物车是空的</p>
        <p class="cart-empty-sub">快去挑选心仪的商品吧</p>
      </div>

      <div class="cart-footer" v-if="cartStore.items.length > 0">
        <div class="cart-total-row">
          <span class="cart-total-label">合计</span>
          <span class="cart-total-price">{{ formatPrice(cartStore.totalPrice) }}</span>
        </div>
        <button class="cart-checkout-btn" @click="handleCheckout">结算</button>
      </div>
    </aside>
  </Teleport>
</template>

<style scoped>
.cart-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 200;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s ease;
}
.cart-overlay.open {
  opacity: 1;
  pointer-events: auto;
}
.cart-sidebar {
  position: fixed;
  top: 0;
  right: 0;
  height: 100vh;
  width: 420px;
  max-width: 100vw;
  background: white;
  z-index: 201;
  display: flex;
  flex-direction: column;
  transform: translateX(100%);
  transition: transform 0.35s ease;
  box-shadow: -8px 0 24px rgba(0, 0, 0, 0.08);
}
.cart-sidebar.open {
  transform: translateX(0);
}
.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border);
}
.cart-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary);
  margin: 0;
}
.cart-close {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-muted);
  padding: 4px;
  border-radius: 4px;
  transition: color 0.2s;
}
.cart-close:hover { color: var(--primary); }
.cart-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.cart-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px;
  background: var(--bg);
  border-radius: 10px;
  border: 1px solid var(--border);
  position: relative;
}
.cart-item-image {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  flex-shrink: 0;
}
.cart-item-detail {
  flex: 1;
  min-width: 0;
}
.cart-item-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cart-item-price {
  font-size: 14px;
  font-weight: 700;
  color: var(--accent);
  margin-bottom: 8px;
}
.cart-item-qty {
  display: flex;
  align-items: center;
  gap: 0;
}
.qty-btn {
  width: 28px;
  height: 28px;
  border: 1px solid var(--border);
  background: white;
  cursor: pointer;
  font-size: 14px;
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.qty-btn:first-child { border-radius: 6px 0 0 6px; }
.qty-btn:last-child { border-radius: 0 6px 6px 0; }
.qty-btn:hover { background: var(--accent); color: white; border-color: var(--accent); }
.qty-value {
  width: 36px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
  font-size: 13px;
  font-weight: 600;
  color: var(--primary);
  background: white;
}
.cart-item-remove {
  position: absolute;
  top: 8px;
  right: 8px;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-muted);
  padding: 2px;
  border-radius: 4px;
  transition: color 0.2s;
}
.cart-item-remove:hover { color: #d9534f; }
.cart-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
}
.cart-empty-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--primary);
  margin-top: 16px;
  margin-bottom: 6px;
}
.cart-empty-sub { font-size: 13px; }
.cart-footer {
  padding: 20px 24px;
  border-top: 1px solid var(--border);
  background: white;
}
.cart-total-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 16px;
}
.cart-total-label { font-size: 15px; color: var(--text-muted); }
.cart-total-price { font-size: 22px; font-weight: 700; color: var(--accent); }
.cart-checkout-btn {
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 28px;
  background: var(--primary);
  color: white;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 1px;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.3s ease;
}
.cart-checkout-btn:hover {
  background: #1a1a1a;
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}
</style>
