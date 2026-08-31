import request from './request'
import type { Product, Order, UserInfo, PageData } from '@/types'

// Product management
export function getAdminProductsApi(params: { page?: number; size?: number; keyword?: string }) {
  return request.get<any, PageData<Product>>('/admin/products', { params })
}

export function getAdminProductApi(id: number) {
  return request.get<any, Product>('/admin/products/' + id)
}

export function createProductApi(data: any) {
  return request.post('/admin/products', data)
}

export function updateProductApi(id: number, data: any) {
  return request.put('/admin/products/' + id, data)
}

export function updateProductStatusApi(id: number, status: number) {
  return request.put('/admin/products/' + id + '/status', null, { params: { status } })
}

// Image upload
export function uploadImageApi(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/admin/upload', formData)
}

// Order management
export function getAdminOrdersApi(params: { page?: number; size?: number; userId?: number; status?: string; orderNo?: string }) {
  return request.get<any, PageData<Order>>('/admin/orders', { params })
}

export function getAdminOrderDetailApi(id: number) {
  return request.get<any, Order>('/admin/orders/' + id)
}

export function updateOrderStatusApi(id: number, status: string, trackingNo?: string, logisticsCompany?: string) {
  return request.put('/admin/orders/' + id + '/status', { status, trackingNo, logisticsCompany })
}

// User management
export function getAdminUsersApi(params: { page?: number; size?: number }) {
  return request.get<any, PageData<UserInfo>>('/admin/users', { params })
}

export function updateUserStatusApi(id: number, status: number) {
  return request.put('/admin/users/' + id + '/status', null, { params: { status } })
}

// Category
export function getCategoriesApi() {
  return request.get<any, { id: number; name: string }[]>('/categories')
}

// Dashboard
export interface DashboardStats {
  todayOrders: number
  weekOrders: number
  monthOrders: number
  todayRevenue: number
  weekRevenue: number
  monthRevenue: number
  todayOrdersTrend: number | null
  weekOrdersTrend: number | null
  monthOrdersTrend: number | null
  todayRevenueTrend: number | null
  weekRevenueTrend: number | null
  monthRevenueTrend: number | null
  topProducts: Array<{ productName: string; totalQuantity: number; totalRevenue: number }>
}

export function getDashboardStatsApi() {
  return request.get<any, DashboardStats>('/admin/dashboard/stats')
}

export function exportReportApi() {
  return request.get('/admin/dashboard/export', { responseType: 'blob' })
}

// Refund management
export interface Refund {
  id: number
  orderId: number
  userId: number
  reason: string
  amount: number
  status: string
  adminNote: string
  createTime: string
}

export function getAdminRefundsApi(params: { page?: number; size?: number; status?: string }) {
  return request.get<any, PageData<Refund>>('/admin/refunds', { params })
}

export function approveRefundApi(id: number, adminNote?: string) {
  return request.put('/admin/refunds/' + id + '/approve', { adminNote: adminNote || '' })
}

export function rejectRefundApi(id: number, adminNote?: string) {
  return request.put('/admin/refunds/' + id + '/reject', { adminNote: adminNote || '' })
}

export function confirmRefundedApi(id: number) {
  return request.put('/admin/refunds/' + id + '/refunded')
}

// Coupon management
export interface Coupon {
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

export function getAdminCouponsApi(params: { page?: number; size?: number }) {
  return request.get<any, PageData<Coupon>>('/admin/coupons', { params })
}

export function createCouponApi(data: Coupon) {
  return request.post('/admin/coupons', data)
}
