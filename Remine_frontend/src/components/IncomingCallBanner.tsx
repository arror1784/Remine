import { useEffect, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { endCall, getActiveCall, type Call } from '@/api/call'
import { getPairedProfile } from '@/api/family'
import type { UserResponse } from '@/api/family'
import { useAuthStore } from '@/store/auth'
import { ROLE_COLOR } from '@/pages/onboarding/types'

const ROLE_STYLE: Record<UserResponse['role'], { emoji: string; color: string }> = {
  PARENT: { emoji: '👩', color: ROLE_COLOR.parent },
  CHILD: { emoji: '👧', color: ROLE_COLOR.child },
}

// Same polling constraint as CallScreen — no push channel exists, so the
// only way to learn "someone is calling me" while I'm on some other screen
// is to keep asking. Mounted once at the App level (not per-page) so it can
// interrupt whatever screen I'm actually on, like a real incoming-call UI.
const POLL_INTERVAL_MS = 2500

export default function IncomingCallBanner() {
  const navigate = useNavigate()
  const location = useLocation()
  const activeRole = useAuthStore((s) => s.activeRole)
  const myUserId = useAuthStore((s) => s.sessions[s.activeRole]?.userId)
  const [incoming, setIncoming] = useState<Call | null>(null)
  const [caller, setCaller] = useState<UserResponse | null>(null)

  // The call screen answers on its own by landing there — showing the
  // banner on top of it too would be redundant.
  const onCallScreen = location.pathname === `/${activeRole}/family/call`

  useEffect(() => {
    if (!myUserId) return
    let cancelled = false
    const poll = () => {
      getActiveCall()
        .then((call) => {
          if (cancelled) return
          setIncoming(call && call.calleeId === myUserId && call.status === 'CONNECTING' ? call : null)
        })
        .catch(() => {})
    }
    poll()
    const interval = setInterval(poll, POLL_INTERVAL_MS)
    return () => {
      cancelled = true
      clearInterval(interval)
    }
  }, [myUserId])

  useEffect(() => {
    if (!incoming) {
      setCaller(null)
      return
    }
    let cancelled = false
    getPairedProfile()
      .then((profile) => {
        if (!cancelled) setCaller(profile)
      })
      .catch(() => {})
    return () => {
      cancelled = true
    }
  }, [incoming])

  if (!incoming || onCallScreen) return null

  const style = caller ? ROLE_STYLE[caller.role] : null

  const decline = () => {
    endCall(incoming.id).catch(() => {})
    setIncoming(null)
  }

  const answer = () => {
    navigate(`/${activeRole}/family/call`)
  }

  return (
    <div className="absolute inset-x-0 top-0 z-[60] px-3 pt-[max(12px,env(safe-area-inset-top))]">
      <div className="flex items-center gap-3 rounded-2xl bg-remine-nearBlack2 px-4 py-3 shadow-lg">
        <div
          className="flex size-11 shrink-0 items-center justify-center rounded-full border-2 bg-remine-highlight text-lg"
          style={{ borderColor: style?.color }}
        >
          {style?.emoji ?? '📞'}
        </div>
        <div className="flex-1 overflow-hidden">
          <p className="truncate text-[15px] font-semibold text-white">{caller?.name ?? '전화'}</p>
          <p className="text-[12px] text-white/50">전화가 왔어요</p>
        </div>
        <button
          type="button"
          onClick={decline}
          className="flex size-10 shrink-0 items-center justify-center rounded-full bg-remine-dangerStrong text-white"
        >
          ✕
        </button>
        <button
          type="button"
          onClick={answer}
          className="flex size-10 shrink-0 items-center justify-center rounded-full bg-remine-teal text-white"
        >
          📞
        </button>
      </div>
    </div>
  )
}
