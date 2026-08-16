import { http } from '@/api/http'

interface ApiEnvelope<T> {
  data: T | null
  error: { code: string; message: string } | null
}

export type ActivityActionType = 'WALK' | 'CALL' | 'QUIZ' | 'NONE'

export interface ActivityRecommendation {
  id: string
  userId: string
  statDate: string
  parentMessage: string
  childMessage: string
  actionType: ActivityActionType
}

function unwrap<T>(envelope: ApiEnvelope<T>): T {
  if (envelope.error) throw new Error(envelope.error.message)
  if (!envelope.data) throw new Error('Empty response')
  return envelope.data
}

export async function getRecommendation(): Promise<ActivityRecommendation> {
  const response = await http.get<ApiEnvelope<ActivityRecommendation>>('/api/v1/activities/recommendation')
  return unwrap(response.data)
}
