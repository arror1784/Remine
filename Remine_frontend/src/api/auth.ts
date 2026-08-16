import { http, type ApiEnvelope, unwrap } from '@/api/http'
import type { Role } from '@/store/auth'

interface DemoLoginResponse {
  userId: string
  role: 'PARENT' | 'CHILD'
  name: string
  accessToken: string
  pairedUserId: string | null
}

interface PairingResponse {
  parentUserId: string
  accessToken: string
}

interface SignUpResponse {
  userId: string
  inviteCode: string | null
  accessToken: string
}

export async function demoLogin(role: Role): Promise<{
  userId: string
  role: Role
  name: string
  accessToken: string
  pairedUserId: string | null
}> {
  const response = await http.post<ApiEnvelope<DemoLoginResponse>>('/api/v1/auth/demo-login', {
    role: role.toUpperCase(),
  })
  const data = unwrap(response.data)
  return { ...data, role: data.role.toLowerCase() as Role }
}

export async function pairWithInviteCode(inviteCode: string): Promise<PairingResponse> {
  const response = await http.post<ApiEnvelope<PairingResponse>>('/api/v1/users/me/pairing', {
    inviteCode,
  })
  return unwrap(response.data)
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
