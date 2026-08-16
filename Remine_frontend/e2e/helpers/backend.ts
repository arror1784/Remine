import { expect, type APIRequestContext } from '@playwright/test'
import { API_BASE_URL } from '../../playwright.config'
import type { TodaySummary } from '@/api/activity'
import type { FamilySummary } from '@/api/family'
import type { MemoryPhoto } from '@/api/memory'
import type { ChatMessage } from '@/api/message'

export type { ChatMessage, FamilySummary, MemoryPhoto, TodaySummary }

export type DemoRole = 'PARENT' | 'CHILD'

// Specs assert the rendered screen against whatever the backend actually
// returned on this run, so seeded values can drift without breaking the suite.
async function demoToken(request: APIRequestContext, role: DemoRole): Promise<string> {
  const response = await request.post(`${API_BASE_URL}/api/v1/auth/demo-login`, { data: { role } })
  expect(response.status(), `demo-login(${role}) should succeed`).toBe(200)
  const body = await response.json()
  expect(body.error, `demo-login(${role}) should not return an error envelope`).toBeNull()
  return body.data.accessToken as string
}

async function apiGet<T>(request: APIRequestContext, token: string, path: string): Promise<T> {
  const response = await request.get(`${API_BASE_URL}${path}`, {
    headers: { Authorization: `Bearer ${token}` },
  })
  expect(response.status(), `GET ${path} should succeed`).toBe(200)
  const body = await response.json()
  expect(body.error, `GET ${path} should not return an error envelope`).toBeNull()
  return body.data as T
}

export async function backendAs(request: APIRequestContext, role: DemoRole) {
  const token = await demoToken(request, role)
  return {
    token,
    todaySummary: () => apiGet<TodaySummary>(request, token, '/api/v1/activities/today'),
    memoryGallery: () => apiGet<MemoryPhoto[]>(request, token, '/api/v1/memories'),
    familySummary: () => apiGet<FamilySummary>(request, token, '/api/v1/family/summary'),
    messageThread: (limit = 50) =>
      apiGet<ChatMessage[]>(request, token, `/api/v1/messages/thread?limit=${limit}`),
  }
}
