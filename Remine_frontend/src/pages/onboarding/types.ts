export type Role = 'parent' | 'child'

export const ROLE_COLOR: Record<Role, string> = {
  parent: '#ff42ad',
  child: '#37ceff',
}

export const PAIRING_FAILED = 'PAIRING_FAILED'

export const AGE_GROUPS = ['10대', '20대', '30대', '40대', '50대', '60대', '70대', '80대', '기타'] as const

export const PARENT_INTERESTS = ['걷기·산책', '수면 관리', '가족 소통', '두뇌 활동', '식사 관리', '사회 활동'] as const

export type OnboardingState = {
  role: Role | null
  name: string
  ageGroup: string | null
  interests: string[]
  inviteCode: string
}
