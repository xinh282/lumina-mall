import request from './request'
import type { PageData } from '@/types'

export interface Review {
  id: number
  productId: number
  userId: number
  orderId: number | null
  rating: number
  content: string
  createTime: string
}

export interface RatingStats {
  avgRating: number
  total: number
}

export function getReviewsApi(productId: number, page = 1, size = 10) {
  return request.get<any, PageData<Review>>('/reviews/product/' + productId, { params: { page, size } })
}

export function getRatingStatsApi(productId: number) {
  return request.get<any, RatingStats>('/reviews/product/' + productId + '/stats')
}

export function createReviewApi(data: { productId: number; orderId?: number; rating: number; content: string }) {
  return request.post('/reviews', data)
}
