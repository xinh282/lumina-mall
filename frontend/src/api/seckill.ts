import request from './request'
import type { Product } from '@/types'

export function getSeckillProductsApi() {
  return request.get<any, Product[]>('/seckill/products')
}

export function seckillApi(productId: number) {
  return request.post('/seckill/' + productId)
}
