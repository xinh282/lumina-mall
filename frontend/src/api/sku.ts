import request from './request'

export interface ProductSku {
  id: number
  productId: number
  specs: string
  price: number | null
  stock: number
  skuCode: string
  status: number
}

export function getSkusApi(productId: number) {
  return request.get<any, ProductSku[]>('/skus/product/' + productId)
}
