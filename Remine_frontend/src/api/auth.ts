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

export type DemoVariant = 'EVAL' | 'DEMO'

export async function demoLogin(role: Role, variant: DemoVariant = 'EVAL'): Promise<{
  userId: string
  role: Role
  name: string
  accessToken: string
  pairedUserId: string | null
}> {
  const response = await http.post<ApiEnvelope<DemoLoginResponse>>('/api/v1/auth/demo-login', {
    role: role.toUpperCase(),
    variant,
  })
  const data = unwrap(response.data)
  return { ...data, role: data.role.toLowerCase() as Role }
}

// No-credential admin utility — see the backend's DemoResetController kdoc for why this is safe
// (it can only ever touch one of the two known seed-account pairs, never a real user).
export async function resetDemoData(variant: DemoVariant = 'DEMO'): Promise<void> {
  await http.post('/api/v1/admin/demo/reset', null, { params: { variant } })
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
