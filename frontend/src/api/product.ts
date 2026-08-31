import request from './request'
import type { Product, PageData } from '@/types'

export function getProductListApi(params: { page?: number; size?: number; categoryId?: number; keyword?: string; sort?: string }) {
  return request.get<any, PageData<Product>>('/products', { params })
}

export function getProductDetailApi(id: number) {
  return request.get<any, Product>('/products/' + id)
}

export function getHotProductsApi(limit = 8) {
  return request.get<any, Product[]>('/products/hot', { params: { limit } })
}

export function getNewProductsApi(limit = 8) {
  return request.get<any, Product[]>('/products/new', { params: { limit } })
}
