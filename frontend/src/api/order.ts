import request from './request'
import type { Order, PageData } from '@/types'

export function createOrderApi(data: { receiverName: string; receiverPhone: string; receiverAddress: string; cartItemIds: number[]; userCouponId?: number }) {
  return request.post<any, Order>('/orders', data)
}

export function getOrderListApi(params: { status?: string; page?: number; size?: number }) {
  return request.get<any, PageData<Order>>('/orders', { params })
}

export function getOrderDetailApi(id: number) {
  return request.get<any, Order>('/orders/' + id)
}

export function cancelOrderApi(id: number) {
  return request.put('/orders/' + id + '/cancel')
}

export function confirmReceiptApi(id: number) {
  return request.put('/orders/' + id + '/receive')
}
