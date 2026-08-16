import { http } from '@/api/http'

interface ApiEnvelope<T> {
  data: T | null
  error: { code: string; message: string } | null
}

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

export interface QuizQuestion {
  id: string
  memoryPhotoId: string
  question: string
  options: string[]
  correctOptionIndex: number
  sortOrder: number
}

function unwrap<T>(envelope: ApiEnvelope<T>): T {
  if (envelope.error) throw new Error(envelope.error.message)
  if (!envelope.data) throw new Error('Empty response')
  return envelope.data
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
