<script setup lang="ts">
import type { Product } from '@/types'
import ProductCard from '@/components/product/ProductCard.vue'
defineProps<{ products: Product[] }>()
const emit = defineEmits<{ 'add-to-cart': [id: number] }>()
</script>
<template>
  <div class="products-grid" v-if="products.length">
    <ProductCard v-for="p in products" :key="p.id" :product="p" @add-to-cart="emit('add-to-cart', $event)" />
  </div>
  <div v-else class="empty">暂无商品</div>
</template>
<style scoped>
.products-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 18px; }
.empty { text-align: center; color: var(--text-muted); padding: 48px 24px; font-size: 14px; }
@media (max-width: 1200px) { .products-grid { grid-template-columns: repeat(4, 1fr); gap: 16px; } }
@media (max-width: 768px) { .products-grid { grid-template-columns: repeat(3, 1fr); gap: 12px; } }
@media (max-width: 480px) { .products-grid { grid-template-columns: repeat(2, 1fr); gap: 10px; } }
</style>
