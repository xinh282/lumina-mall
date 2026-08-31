import request from './request'
import type { CartItem } from '@/types'

export function getCartListApi() {
  return request.get<any, CartItem[]>('/cart')
}

export function addToCartApi(data: { productId: number; quantity: number }) {
  return request.post('/cart', data)
}

export function updateCartItemApi(id: number, quantity: number) {
  return request.put('/cart/' + id, null, { params: { quantity } })
}

export function removeCartItemApi(id: number) {
  return request.delete('/cart/' + id)
}

export function clearCartApi() {
  return request.delete('/cart')
}
