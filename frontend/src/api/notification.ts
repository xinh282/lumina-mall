import request from './request'

export interface Notification {
  id: number
  userId: number
  title: string
  content: string
  isRead: number
  type: string
  refId: number
  createTime: string
}

export function getNotificationsApi() {
  return request.get<any, Notification[]>('/notifications')
}

export function getUnreadCountApi() {
  return request.get<any, { count: number }>('/notifications/unread-count')
}

export function markReadApi(id: number) {
  return request.put('/notifications/' + id + '/read')
}

export function markAllReadApi() {
  return request.put('/notifications/read-all')
}
