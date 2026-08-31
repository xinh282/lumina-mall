import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types'
import { loginApi, getProfileApi, updateProfileApi } from '@/api/auth'
import { getToken, setToken, removeToken, setStorage, getStorage } from '@/utils/storage'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const userInfo = ref<UserInfo | null>(getStorage<UserInfo>('userInfo'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  async function login(username: string, password: string) {
    const res = await loginApi(username, password)
    token.value = res.token
    userInfo.value = res.userInfo
    setToken(res.token)
    setStorage('userInfo', res.userInfo)
  }

  function logout() {
    token.value = null
    userInfo.value = null
    removeToken()
    localStorage.removeItem('userInfo')
  }

  async function fetchProfile() {
    const res = await getProfileApi()
    userInfo.value = res
    setStorage('userInfo', res)
  }

  async function updateProfile(data: Partial<UserInfo>) {
    await updateProfileApi(data)
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...data }
      setStorage('userInfo', userInfo.value)
    }
  }

  function setAvatar(url: string) {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, avatar: url }
      setStorage('userInfo', userInfo.value)
    }
    // 异步同步到后端
    updateProfileApi({ avatar: url } as any).catch(() => {})
  }

  return { token, userInfo, isLoggedIn, isAdmin, login, logout, fetchProfile, updateProfile, setAvatar }
})
