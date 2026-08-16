import { useEffect, useState } from 'react'
import CallScreen from '@/components/CallScreen'
import { getPairedProfile } from '@/api/family'
import type { UserResponse } from '@/api/family'
import { ROLE_COLOR } from '@/pages/onboarding/types'

const ROLE_STYLE: Record<UserResponse['role'], { emoji: string; color: string }> = {
  PARENT: { emoji: '👩', color: ROLE_COLOR.parent },
  CHILD: { emoji: '👧', color: ROLE_COLOR.child },
}

export default function ParentCall() {
  const [paired, setPaired] = useState<UserResponse | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    getPairedProfile()
      .then((counterpart) => {
        if (!cancelled) setPaired(counterpart)
      })
      .catch(() => {})
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  if (loading || !paired) {
    return (
      <div className="flex h-full min-h-[600px] flex-col items-center justify-center gap-2 bg-gradient-to-b from-remine-nearBlack2 to-remine-deepPurple px-6 text-center">
        {loading ? (
          <p className="text-[14px] text-white/50">불러오는 중...</p>
        ) : (
          <>
            <span className="text-3xl">👨‍👩‍👧</span>
            <p className="text-[16px] font-semibold text-white">아직 연결된 가족이 없어요</p>
            <p className="text-[14px] leading-[1.6] text-white/50">
              자녀가 초대 코드를 입력하면 전화를 걸 수 있어요.
            </p>
          </>
        )}
      </div>
    )
  }

  const style = ROLE_STYLE[paired.role]

  return (
    <CallScreen
      name={paired.name}
      relation={paired.role === 'CHILD' ? '자녀' : '가족'}
      emoji={style.emoji}
      accentColor={style.color}
      backTo="/parent/family"
    />
  )
}
