import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { answerCall, endCall, getActiveCall, startCall } from '@/api/call'
import { useAuthStore } from '@/store/auth'

type CallScreenProps = {
  name: string
  relation: string
  emoji: string
  accentColor: string
  backTo: string
}

type CallState = 'connecting' | 'connected' | 'ended'

// No websocket/push infra exists yet (same constraint as the message thread),
// so both sides learn about each other's answer/hangup by polling the shared
// call-log row instead of a real signaling channel.
const POLL_INTERVAL_MS = 2000

export default function CallScreen({ name, relation, emoji, accentColor, backTo }: CallScreenProps) {
  const navigate = useNavigate()
  const [state, setState] = useState<CallState>('connecting')
  const [seconds, setSeconds] = useState(0)
  const callIdRef = useRef<string | null>(null)
  const endedRef = useRef(false)
  const myUserId = useAuthStore((s) => s.sessions[s.activeRole]?.userId)

  const finishCall = useCallback(() => {
    const id = callIdRef.current
    if (!id || endedRef.current) return
    endedRef.current = true
    endCall(id).catch(() => {})
  }, [])

  // Attach to whatever call is already active for me instead of always
  // starting a fresh one: if I'm the callee of a still-ringing call, landing
  // here (via the incoming-call banner's "수락", or by tapping the call icon
  // directly) is the "answer" action. If I'm the caller, or I've re-entered
  // an already-connected call, just observe it.
  useEffect(() => {
    let cancelled = false
    const init = async () => {
      try {
        const active = await getActiveCall()
        if (cancelled) return
        if (active) {
          callIdRef.current = active.id
          if (active.calleeId === myUserId && active.status === 'CONNECTING') {
            const answered = await answerCall(active.id)
            if (cancelled) return
            setState(answered.status === 'CONNECTED' ? 'connected' : 'connecting')
          } else {
            setState(active.status === 'CONNECTED' ? 'connected' : 'connecting')
          }
        } else {
          const call = await startCall()
          if (cancelled) {
            // Unmounted before the call was registered — close it right away.
            endCall(call.id).catch(() => {})
            return
          }
          callIdRef.current = call.id
        }
      } catch {
        if (!cancelled) navigate(backTo)
      }
    }
    init()
    return () => {
      cancelled = true
      finishCall()
    }
  }, [backTo, navigate, finishCall, myUserId])

  // Learn about the other participant's answer or hangup.
  useEffect(() => {
    if (state === 'ended') return
    const interval = setInterval(() => {
      getActiveCall()
        .then((active) => {
          if (!active || active.id !== callIdRef.current || active.status === 'ENDED' || active.status === 'MISSED') {
            setState('ended')
          } else if (active.status === 'CONNECTED') {
            setState((prev) => (prev === 'connecting' ? 'connected' : prev))
          }
        })
        .catch(() => {})
    }, POLL_INTERVAL_MS)
    return () => clearInterval(interval)
  }, [state])

  useEffect(() => {
    if (state !== 'connected') return
    const timer = setInterval(() => setSeconds((s) => s + 1), 1000)
    return () => clearInterval(timer)
  }, [state])

  useEffect(() => {
    if (state !== 'ended') return
    finishCall()
    const toBack = setTimeout(() => navigate(backTo), 1500)
    return () => clearTimeout(toBack)
  }, [state, backTo, navigate, finishCall])

  const mm = String(Math.floor(seconds / 60)).padStart(2, '0')
  const ss = String(seconds % 60).padStart(2, '0')

  const hangUp = () => setState('ended')

  return (
    <div className="flex h-full min-h-[600px] flex-col items-center justify-between bg-gradient-to-b from-remine-nearBlack2 to-remine-deepPurple px-6 pb-10 pt-16 sm:pt-6">
      <div className="flex flex-col items-center gap-1">
        {state === 'connecting' && <p className="text-[14px] text-white/50">전화 연결 중...</p>}
        {state === 'connected' && (
          <>
            <p className="text-[14px] text-white/50">통화 중</p>
            <p className="text-[16px] font-semibold" style={{ color: accentColor }}>
              {mm}:{ss}
            </p>
          </>
        )}
      </div>

      <div className="flex flex-col items-center gap-6">
        <div className="relative flex items-center justify-center">
          {state !== 'ended' && (
            <>
              <span className="absolute size-[220px] rounded-full border" style={{ borderColor: `${accentColor}30` }} />
              <span className="absolute size-[170px] rounded-full border" style={{ borderColor: `${accentColor}50` }} />
            </>
          )}
          <div
            className="flex size-[110px] items-center justify-center rounded-full border-2 bg-remine-highlight text-4xl"
            style={{ borderColor: accentColor }}
          >
            {emoji}
          </div>
        </div>
        <div className="flex flex-col items-center gap-1">
          <p className="text-[24px] font-semibold text-white">{name}</p>
          <p className="text-[14px] text-white/50">{relation}</p>
          {state === 'ended' && <p className="pt-3 text-[14px] text-white/50">통화가 종료되었어요</p>}
        </div>
      </div>

      {state !== 'ended' ? (
        <div className="flex items-center gap-8">
          <button type="button" className="flex size-14 items-center justify-center rounded-full bg-white/10 text-lg text-white">
            🔇
          </button>
          <button
            type="button"
            onClick={hangUp}
            className="flex size-16 items-center justify-center rounded-full bg-remine-dangerStrong text-2xl text-white shadow-[0_0_20px_rgba(229,73,63,0.5)]"
          >
            📞
          </button>
          <button type="button" className="flex size-14 items-center justify-center rounded-full bg-white/10 text-lg text-white">
            🔊
          </button>
        </div>
      ) : (
        <div />
      )}
    </div>
  )
}
