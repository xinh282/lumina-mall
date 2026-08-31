import request from './request'
import type { Refund } from './admin'
import type { PageData } from '@/types'

export function applyRefundApi(orderId: number, reason: string) {
  return request.post('/refunds', { orderId, reason })
}

export function getMyRefundsApi(params: { page?: number; size?: number }) {
  return request.get<any, PageData<Refund>>('/refunds', { params })
}
