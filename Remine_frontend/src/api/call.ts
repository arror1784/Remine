import { http, type ApiEnvelope, unwrap } from '@/api/http'

export interface Call {
  id: string
  callerId: string
  calleeId: string
  status: 'CONNECTING' | 'CONNECTED' | 'ENDED' | 'MISSED'
  startedAt: string
  endedAt: string | null
  durationSeconds: number
  createdAt: string
  updatedAt: string
}

// The callee defaults server-side to the caller's paired counterpart.
export async function startCall(): Promise<Call> {
  const response = await http.post<ApiEnvelope<Call>>('/api/v1/calls', {})
  return unwrap(response.data)
}

export async function answerCall(callId: string): Promise<Call> {
  const response = await http.patch<ApiEnvelope<Call>>(`/api/v1/calls/${callId}/answer`)
  return unwrap(response.data)
}

export async function endCall(callId: string): Promise<Call> {
  const response = await http.patch<ApiEnvelope<Call>>(`/api/v1/calls/${callId}/end`)
  return unwrap(response.data)
}

// A null `data` is a valid answer here (no active call right now), so this
// skips `unwrap` the same way api/family.ts's getPairedProfile does.
export async function getActiveCall(): Promise<Call | null> {
  const response = await http.get<ApiEnvelope<Call>>('/api/v1/calls/active')
  if (response.data.error) throw new Error(response.data.error.message)
  return response.data.data
}
