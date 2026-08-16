import { http } from '@/api/http'
import type { Role } from '@/store/auth'

interface ApiEnvelope<T> {
  data: T | null
  error: { code: string; message: string } | null
}

interface DemoLoginResponse {
  userId: string
  role: 'PARENT' | 'CHILD'
  accessToken: string
  pairedUserId: string | null
}

interface SignUpResponse {
  userId: string
  inviteCode: string | null
  accessToken: string
}

function unwrap<T>(envelope: ApiEnvelope<T>): T {
  if (envelope.error) throw new Error(envelope.error.message)
  if (!envelope.data) throw new Error('Empty response')
  return envelope.data
}

export async function demoLogin(role: Role): Promise<{
  userId: string
  role: Role
  accessToken: string
  pairedUserId: string | null
}> {
  const response = await http.post<ApiEnvelope<DemoLoginResponse>>('/api/v1/auth/demo-login', {
    role: role.toUpperCase(),
  })
  const data = unwrap(response.data)
  return { ...data, role: data.role.toLowerCase() as Role }
}

export async function signUp(payload: {
  role: Role
  name: string
  ageGroup: string
  interests: string[]
}): Promise<SignUpResponse> {
  const response = await http.post<ApiEnvelope<SignUpResponse>>('/api/v1/users/signup', {
    role: payload.role.toUpperCase(),
    name: payload.name,
    ageGroup: payload.ageGroup,
    interests: payload.interests,
  })
  return unwrap(response.data)
}
