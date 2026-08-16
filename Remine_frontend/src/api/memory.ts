import { http, type ApiEnvelope, unwrap } from '@/api/http'

export interface MemoryPhoto {
  id: string
  title: string
  photoUrl: string
  memoryLabel: string
  status: string
  createdAt: string
  updatedAt: string
}

export interface DraftQuestion {
  id: string
  memoryPhotoId: string
  question: string
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export interface TodayQuizQuestion {
  id: string
  question: string
  options: string[]
}

export interface TodayQuiz {
  photo: MemoryPhoto | null
  questions: TodayQuizQuestion[]
}

export interface QuizAttemptResult {
  correctCount: number
  totalCount: number
}

export interface QuizQuestion {
  id: string
  memoryPhotoId: string
  question: string
  options: string[]
  correctOptionIndex: number
  sortOrder: number
}

export async function getMemoryGallery(): Promise<MemoryPhoto[]> {
  const response = await http.get<ApiEnvelope<MemoryPhoto[]>>('/api/v1/memories')
  return unwrap(response.data)
}

export async function uploadPhoto(payload: {
  title: string
  photoUrl: string
  memoryLabel: string
}): Promise<MemoryPhoto> {
  const response = await http.post<ApiEnvelope<MemoryPhoto>>('/api/v1/memories', payload)
  return unwrap(response.data)
}

export async function generateQuizQuestions(photoId: string): Promise<DraftQuestion[]> {
  const response = await http.post<ApiEnvelope<DraftQuestion[]>>(
    `/api/v1/memories/${photoId}/quiz/generate-questions`
  )
  return unwrap(response.data)
}

export async function getDraftQuestions(photoId: string): Promise<DraftQuestion[]> {
  const response = await http.get<ApiEnvelope<DraftQuestion[]>>(
    `/api/v1/memories/${photoId}/quiz/draft-questions`
  )
  return unwrap(response.data)
}

export async function getTodayQuiz(): Promise<TodayQuiz> {
  const response = await http.get<ApiEnvelope<TodayQuiz>>('/api/v1/memories/quiz/today')
  return unwrap(response.data)
}

export async function submitQuizAttempt(
  photoId: string,
  answers: number[]
): Promise<QuizAttemptResult> {
  const response = await http.post<ApiEnvelope<QuizAttemptResult>>(
    `/api/v1/memories/${photoId}/quiz/attempts`,
    { answers }
  )
  return unwrap(response.data)
}

export async function completeQuizWithAnswers(
  photoId: string,
  answers: { questionId: string; answer: string }[]
): Promise<QuizQuestion[]> {
  const response = await http.post<ApiEnvelope<QuizQuestion[]>>(
    `/api/v1/memories/${photoId}/quiz/complete-with-answers`,
    { answers }
  )
  return unwrap(response.data)
}
