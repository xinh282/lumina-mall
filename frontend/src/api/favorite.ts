import request from './request'
import type { Product } from '@/types'

export function getFavoritesApi() {
  return request.get<any, Product[]>('/favorites')
}

export function checkFavoriteApi(productId: number) {
  return request.get<any, boolean>('/favorites/check/' + productId)
}

export function toggleFavoriteApi(productId: number) {
  return request.post<any, boolean>('/favorites/toggle/' + productId)
}
