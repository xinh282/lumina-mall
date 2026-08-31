export function getToken(): string | null {
  return localStorage.getItem('token')
}

export function setToken(token: string): void {
  localStorage.setItem('token', token)
}

export function removeToken(): void {
  localStorage.removeItem('token')
}

export function getStorage<T>(key: string): T | null {
  const val = localStorage.getItem(key)
  if (!val) return null
  try {
    return JSON.parse(val)
  } catch {
    return null
  }
}

export function setStorage(key: string, value: any): void {
  localStorage.setItem(key, JSON.stringify(value))
}
