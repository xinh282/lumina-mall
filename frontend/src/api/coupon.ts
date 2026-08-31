import request from './request'

export interface UserCoupon {
  id: number
  name: string
  type: string
  threshold: number
  discountValue: number
  expireDays: number
}

export interface AvailableCoupon {
  id: number
  name: string
  type: string
  threshold: number
  discountValue: number
  totalCount: number
  usedCount: number
  expireDays: number
  status: number
}

export function getAvailableCouponsApi() {
  return request.get<any, AvailableCoupon[]>('/coupons/available')
}

export function claimCouponApi(couponId: number) {
  return request.post('/coupons/' + couponId + '/claim')
}

export function getMyCouponsApi() {
  return request.get<any, UserCoupon[]>('/coupons')
}

export function calcDiscountApi(userCouponId: number, amount: number) {
  return request.get<any, { discount: number; finalAmount: number }>(
    '/coupons/' + userCouponId + '/discount',
    { params: { amount } }
  )
}
