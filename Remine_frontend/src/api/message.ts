import { http, type ApiEnvelope, unwrap } from '@/api/http'

export interface ChatMessage {
  id: string
  senderId: string
  recipientId: string
  body: string
  quickReplyKey: string | null
  createdAt: string
}

export interface QuickReply {
  id: string
  role: string
  label: string
  sortOrder: number
}

// The server returns newest-first; chat display wants oldest-first.
export async function getThread(before?: string): Promise<ChatMessage[]> {
  const response = await http.get<ApiEnvelope<ChatMessage[]>>('/api/v1/messages/thread', {
    params: { before, limit: 50 },
  })
  return unwrap(response.data).slice().reverse()
}

export async function sendMessage(text: string, quickReplyKey?: string): Promise<ChatMessage> {
  const response = await http.post<ApiEnvelope<ChatMessage>>('/api/v1/messages', { text, quickReplyKey })
  return unwrap(response.data)
}

export async function getQuickReplies(): Promise<QuickReply[]> {
  const response = await http.get<ApiEnvelope<QuickReply[]>>('/api/v1/messages/quick-replies')
  return unwrap(response.data)
}
