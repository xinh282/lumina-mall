import request from './request'

export interface ProductRef {
  id: number
  name: string
  image: string
  price: number
}

export interface ChatResponse {
  reply: string
  usedTool: string | null
  products: ProductRef[] | null
}

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

export function chatApi(message: string, history: ChatMessage[]) {
  return request.post<any, ChatResponse>('/ai/chat', { message, history })
}
