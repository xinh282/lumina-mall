import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { CartItem } from '@/types'
import { getCartListApi, addToCartApi, updateCartItemApi, removeCartItemApi, clearCartApi } from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])

  const totalCount = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
  const totalPrice = computed(() => items.value.reduce((sum, item) => sum + item.productPrice * item.quantity, 0))

  async function fetchCart() {
    const res = await getCartListApi()
    items.value = res
  }

  async function addToCart(productId: number, quantity: number, skuId?: number) {
    await addToCartApi({ productId, quantity, skuId } as any)
    await fetchCart()
  }

  async function updateQuantity(id: number, quantity: number) {
    await updateCartItemApi(id, quantity)
    const item = items.value.find((i) => i.id === id)
    if (item) {
      item.quantity = quantity
    }
  }

  async function removeItem(id: number) {
    await removeCartItemApi(id)
    items.value = items.value.filter((i) => i.id !== id)
  }

  async function clearCart() {
    await clearCartApi()
    items.value = []
  }

  return { items, totalCount, totalPrice, fetchCart, addToCart, updateQuantity, removeItem, clearCart }
})
