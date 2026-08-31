import request from './request'
import type { Category } from '@/types'

export function getCategoriesApi() {
  return request.get<any, Category[]>('/categories')
}
