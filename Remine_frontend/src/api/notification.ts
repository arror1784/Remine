import { http } from '@/api/http'

interface ApiEnvelope<T> {
  data: T | null
  error: { code: string; message: string } | null
}

export interface NotificationItem {
  id: string
  emoji: string
  bgColor: string
  title: string
  description: string
  deepLink: string
  read: boolean
  createdAt: string
}

function unwrap<T>(envelope: ApiEnvelope<T>): T {
  if (envelope.error) throw new Error(envelope.error.message)
  if (!envelope.data) throw new Error('Empty response')
  return envelope.data
}

export async function getNotifications(): Promise<NotificationItem[]> {
  const response = await http.get<ApiEnvelope<NotificationItem[]>>('/api/v1/notifications')
  return unwrap(response.data)
}

export async function markNotificationAsRead(id: string): Promise<NotificationItem> {
  const response = await http.patch<ApiEnvelope<NotificationItem>>(`/api/v1/notifications/${id}/read`)
  return unwrap(response.data)
}

export async function getUnreadCount(): Promise<number> {
  const response = await http.get<ApiEnvelope<{ count: number }>>('/api/v1/notifications/unread-count')
  return unwrap(response.data).count
}
