import request from './request'
import type { Address } from '@/types'

export function getDefaultAddressApi() {
  return request.get<any, Address | null>('/address/default')
}

export function saveAddressApi(data: {
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  saveAsDefault: number
}) {
  return request.post('/address', data)
}
