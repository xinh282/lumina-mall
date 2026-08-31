export function formatPrice(price: number): string {
  return '¥' + price.toLocaleString()
}

export function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

export const orderStatusMap: Record<string, string> = {
  PENDING: '待支付',
  PAID: '已支付',
  SHIPPED: '已发货',
  RECEIVED: '已收货',
  CANCELLED: '已取消'
}

export const statusColorMap: Record<string, string> = {
  PENDING: 'warning',
  PAID: 'success',
  SHIPPED: 'primary',
  RECEIVED: 'success',
  CANCELLED: 'info'
}
