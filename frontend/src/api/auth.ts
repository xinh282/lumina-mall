import request from './request'
import type { UserInfo } from '@/types'

export function loginApi(username: string, password: string) {
  return request.post<any, { token: string; userInfo: UserInfo }>('/user/login', { username, password })
}

export function registerApi(data: { username: string; password: string; confirmPassword: string; nickname?: string }) {
  return request.post('/user/register', data)
}

export function getProfileApi() {
  return request.get<any, UserInfo>('/user/profile')
}

export function updateProfileApi(data: Partial<UserInfo>) {
  return request.put('/user/profile', data)
}
