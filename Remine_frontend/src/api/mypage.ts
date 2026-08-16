import { http } from '@/api/http'

interface ApiEnvelope<T> {
  data: T | null
  error: { code: string; message: string } | null
}

export interface MyPageStats {
  streakDays: number
  sharedPhotoCount: number
  quizActiveCount: number
  activeDaysThisWeek: number
  totalDaysThisWeek: number
}

export interface WeeklyPatternDay {
  statDate: string
  steps: number
  isToday: boolean
}

function unwrap<T>(envelope: ApiEnvelope<T>): T {
  if (envelope.error) throw new Error(envelope.error.message)
  if (!envelope.data) throw new Error('Empty response')
  return envelope.data
}

export async function getMyPageStats(): Promise<MyPageStats> {
  const response = await http.get<ApiEnvelope<MyPageStats>>('/api/v1/users/me/stats')
  return unwrap(response.data)
}

export async function getWeeklyPattern(): Promise<WeeklyPatternDay[]> {
  const response = await http.get<ApiEnvelope<{ days: WeeklyPatternDay[] }>>('/api/v1/activities/weekly')
  return unwrap(response.data).days
}
