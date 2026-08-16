import { http, type ApiEnvelope, unwrap } from '@/api/http'

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

export async function getMyPageStats(): Promise<MyPageStats> {
  const response = await http.get<ApiEnvelope<MyPageStats>>('/api/v1/users/me/stats')
  return unwrap(response.data)
}

export async function getWeeklyPattern(): Promise<WeeklyPatternDay[]> {
  const response = await http.get<ApiEnvelope<{ days: WeeklyPatternDay[] }>>('/api/v1/activities/weekly')
  return unwrap(response.data).days
}
