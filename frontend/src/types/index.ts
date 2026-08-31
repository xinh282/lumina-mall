export interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  role: string
  status: number
  createTime: string
}

export interface Product {
  id: number
  name: string
  description: string
  categoryId: number
  categoryName: string
  price: number
  originalPrice: number | null
  stock: number
  badge: string
  badgeText: string
  image: string
  images: string
  sales: number
  status: number
  isHot: number
  isNew: number
  seckillStock: number
  seckillPrice: number | null
  seckillStart: string | null
  seckillEnd: string | null
  sortOrder: number
  createTime: string
}

export interface CartItem {
  id: number
  userId: number
  productId: number
  skuId: number | null
  specs: string | null
  productName: string
  productPrice: number
  productImage: string
  quantity: number
  checked: number
}

export interface OrderItem {
  id: number
  orderId: number
  productId: number
  productName: string
  productPrice: number
  quantity: number
  totalPrice: number
}

export interface Order {
  id: number
  orderNo: string
  userId: number
  totalAmount: number
  status: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  trackingNo: string | null
  logisticsCompany: string | null
  payTime: string | null
  createTime: string
  orderItems: OrderItem[]
}

export interface PageData<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface Address {
  id: number
  userId: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  isDefault: number
}

export interface Category {
  id: number
  name: string
  icon: string
  sortOrder: number
}
