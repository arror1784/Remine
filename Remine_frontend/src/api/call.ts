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

export async function endCall(callId: string): Promise<Call> {
  const response = await http.patch<ApiEnvelope<Call>>(`/api/v1/calls/${callId}/end`)
  return unwrap(response.data)
}
