import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { endCall, startCall } from '@/api/call'

type CallScreenProps = {
  name: string
  relation: string
  emoji: string
  accentColor: string
  backTo: string
}

type CallState = 'connecting' | 'connected' | 'ended'

export default function CallScreen({ name, relation, emoji, accentColor, backTo }: CallScreenProps) {
  const navigate = useNavigate()
  const [state, setState] = useState<CallState>('connecting')
  const [seconds, setSeconds] = useState(0)
  const callIdRef = useRef<string | null>(null)
  const endedRef = useRef(false)

  const finishCall = useCallback(() => {
    const id = callIdRef.current
    if (!id || endedRef.current) return
    endedRef.current = true
    endCall(id).catch(() => {})
  }, [])

  useEffect(() => {
    let cancelled = false
    startCall()
      .then((call) => {
        // Unmounted before the call was registered — close it right away.
        if (cancelled) {
          endCall(call.id).catch(() => {})
          return
        }
        callIdRef.current = call.id
      })
      .catch(() => {
        if (!cancelled) navigate(backTo)
      })
    return () => {
      cancelled = true
      finishCall()
    }
  }, [backTo, navigate, finishCall])

  useEffect(() => {
    if (state !== 'connecting') return
    const toConnected = setTimeout(() => setState('connected'), 2000)
    return () => clearTimeout(toConnected)
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
    <div className="flex h-full min-h-[600px] flex-col items-center justify-between bg-gradient-to-b from-remine-nearBlack2 to-remine-deepPurple px-6 pb-10 pt-16">
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
