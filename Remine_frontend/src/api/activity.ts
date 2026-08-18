import { http, type ApiEnvelope, unwrap } from '@/api/http'

export type ActivityActionType = 'WALK' | 'CALL' | 'QUIZ' | 'NONE'

export interface ActivityRecommendation {
  id: string
  userId: string
  statDate: string
  parentMessage: string
  childMessage: string
  actionType: ActivityActionType
}

export interface DailyActivityStat {
  id: string
  userId: string
  statDate: string
  sleepMinutes: number
  steps: number
  outingCount: number
  socialContactCount: number
  sleepGoalMinutes: number
  stepsGoal: number
  outingGoal: number
  socialGoal: number
}

export interface TodaySummary {
  stat: DailyActivityStat | null
  sleepPercent: number
  stepsPercent: number
  outingPercent: number
  socialPercent: number
}

export type ChecklistItemType = 'SLEEP' | 'BREAKFAST' | 'WALK' | 'QUIZ'

export interface ChecklistItem {
  id: string
  userId: string
  statDate: string
  type: ChecklistItemType
  done: boolean
  completedAt: string | null
  note: string | null
}

export interface TimelineEvent {
  id: string
  userId: string
  statDate: string
  occurredAt: string
  label: string
  colorHint: string | null
}

export async function getRecommendation(): Promise<ActivityRecommendation> {
  const response = await http.get<ApiEnvelope<ActivityRecommendation>>('/api/v1/activities/recommendation')
  return unwrap(response.data)
}

export async function getTodaySummary(): Promise<TodaySummary> {
  const response = await http.get<ApiEnvelope<TodaySummary>>('/api/v1/activities/today')
  return unwrap(response.data)
}

export async function getChecklist(): Promise<ChecklistItem[]> {
  const response = await http.get<ApiEnvelope<ChecklistItem[]>>('/api/v1/activities/checklist')
  return unwrap(response.data)
}

export async function getTimeline(): Promise<TimelineEvent[]> {
  const response = await http.get<ApiEnvelope<TimelineEvent[]>>('/api/v1/activities/timeline')
  return unwrap(response.data)
}
