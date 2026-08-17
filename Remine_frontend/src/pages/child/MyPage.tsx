import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Screen from '@/components/Screen'
import { getFamilySummary, getMyProfile, getPairedProfile } from '@/api/family'
import type { FamilySummary, UserResponse } from '@/api/family'
import { useAuthStore } from '@/store/auth'
import { COLORS } from '@/theme'

const INFO_ITEMS = [
  { emoji: '📋', title: '이용약관' },
  { emoji: '📄', title: '개인정보 처리방침' },
  { emoji: 'ℹ️', title: '버전 정보', value: 'v1.0.0' },
]

export default function ChildMyPage() {
  const navigate = useNavigate()
  const [alerts, setAlerts] = useState({ status: true, message: true, weekly: false })
  const [profile, setProfile] = useState<UserResponse | null>(null)
  const [paired, setPaired] = useState<UserResponse | null>(null)
  const [summary, setSummary] = useState<FamilySummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let cancelled = false
    const load = async () => {
      try {
        const [me, counterpart] = await Promise.all([getMyProfile(), getPairedProfile()])
        if (cancelled) return
        setProfile(me)
        setPaired(counterpart)
        if (counterpart) {
          const stats = await getFamilySummary()
          if (cancelled) return
          setSummary(stats)
        }
      } catch {
        if (!cancelled) setFailed(true)
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => {
      cancelled = true
    }
  }, [])

  const logout = () => {
    useAuthStore.getState().clearSessions()
    navigate('/login')
  }

  const monthStats = [
    { value: summary ? `${summary.sharedPhotoCount}장` : '—', label: '추가한 사진', color: 'text-remine-blue' },
    { value: summary ? `${summary.messageCount}개` : '—', label: '보낸 메시지', color: 'text-remine-pink' },
    { value: summary ? `${summary.callCount}회` : '—', label: '전화 통화', color: 'text-remine-orange' },
  ]

  const accountItems = [
    { emoji: '🔑', title: '부모님 초대 코드 확인', value: paired?.inviteCode ?? '—' },
    { emoji: '👥', title: '다른 가족 초대하기' },
    { emoji: '🔄', title: '연결된 부모님 변경' },
  ]

  const alertItems = [
    { emoji: '🔔', key: 'status' as const, label: '어머니 상태 변화 알림' },
    { emoji: '💬', key: 'message' as const, label: '가족 메시지 알림' },
    { emoji: '📊', key: 'weekly' as const, label: '주간 요약 리포트' },
  ]

  return (
    <Screen>
      <div className="flex items-center justify-between px-5 pt-[max(30px,env(safe-area-inset-top))] sm:pt-4">
        <h1 className="text-[22px] font-semibold text-remine-dark">마이페이지</h1>
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="flex size-9 items-center justify-center rounded-full bg-remine-border text-remine-subtle"
        >
          ✕
        </button>
      </div>

      <div className="flex flex-col gap-6 px-5 pb-10 pt-5">
        <div className="relative overflow-hidden rounded-3xl bg-remine-surfaceDark p-6">
          <div aria-hidden className="absolute -right-5 -top-5 size-[100px] rounded-full bg-remine-blue opacity-10 blur-[15px]" />
          <div className="flex items-center gap-4">
            <div className="flex size-[68px] items-center justify-center rounded-full border-2 border-remine-blue bg-remine-highlight text-2xl">👧</div>
            <div className="flex flex-1 flex-col gap-1">
              <p className="text-[20px] font-semibold text-white">
                {loading ? '불러오는 중...' : profile ? `${profile.name}님` : '자녀'}
              </p>
              <p className="flex items-center gap-1.5 text-[13px] text-white/45">
                <span className="size-1.5 rounded-full bg-remine-blue" /> 자녀 모드
              </p>
            </div>
          </div>
          {!loading && paired && (
            <div className="mt-5 flex items-center gap-3 rounded-2xl bg-white/8 px-4 py-3">
              <span className="flex size-9 items-center justify-center rounded-full bg-remine-highlight text-[14px]">👩</span>
              <div className="flex-1">
                <p className="text-[14px] font-medium text-white">{paired.name}님 연결됨</p>
                <p className="text-[12px] text-white/40">부모님 · {paired.inviteCode ?? '—'}</p>
              </div>
              <span className="rounded-full bg-remine-blue/20 px-3 py-1 text-[12px] font-semibold text-remine-blue">연결됨</span>
            </div>
          )}
          {!loading && !paired && (
            <div className="mt-5 flex flex-col items-center gap-1.5 rounded-2xl bg-white/8 px-4 py-5 text-center">
              <span className="text-xl">👨‍👩‍👧</span>
              <p className="text-[14px] font-semibold text-white">
                {failed ? '불러오지 못했어요' : '아직 연결된 부모님이 없어요'}
              </p>
              {!failed && (
                <p className="text-[12px] leading-[1.6] text-white/45">초대 코드를 입력하면 부모님과 연결할 수 있어요.</p>
              )}
            </div>
          )}
        </div>

        <div className="grid grid-cols-3 gap-2.5">
          {monthStats.map((s) => (
            <div key={s.label} className="flex flex-col items-center gap-1 rounded-[20px] border border-remine-border bg-white py-4">
              <span className={`text-[20px] font-semibold ${s.color}`}>{s.value}</span>
              <span className="text-[12px] text-remine-muted">{s.label}</span>
            </div>
          ))}
        </div>

        <div className="flex flex-col overflow-hidden rounded-[20px] bg-white">
          <p className="px-5 pb-2 pt-4 text-[12px] font-semibold tracking-wide text-remine-muted">알림 설정</p>
          {alertItems.map((row, i) => (
            <div key={row.key} className={`flex items-center gap-3.5 px-5 py-3.5 ${i > 0 ? 'border-t border-remine-surfaceAlt' : ''}`}>
              <div className="flex size-11 items-center justify-center rounded-2xl bg-remine-surface text-[15px]">{row.emoji}</div>
              <span className="flex-1 text-[16px] font-medium text-remine-dark">{row.label}</span>
              <button
                type="button"
                onClick={() => setAlerts((a) => ({ ...a, [row.key]: !a[row.key] }))}
                className="flex h-7 w-12 items-center rounded-full px-1 transition-colors"
                style={{ backgroundColor: alerts[row.key] ? COLORS.blue : COLORS.border }}
              >
                <span
                  className="size-5 rounded-full bg-white shadow transition-transform"
                  style={{ transform: alerts[row.key] ? 'translateX(20px)' : 'translateX(0)' }}
                />
              </button>
            </div>
          ))}
        </div>

        <div className="flex flex-col overflow-hidden rounded-[20px] bg-white">
          <p className="px-5 pb-2 pt-4 text-[12px] font-semibold tracking-wide text-remine-muted">계정</p>
          {accountItems.map((item, i) => (
            <div key={item.title} className={`flex items-center gap-3.5 px-5 py-3.5 ${i > 0 ? 'border-t border-remine-surfaceAlt' : ''}`}>
              <div className="flex size-11 items-center justify-center rounded-2xl bg-remine-surface text-[15px]">{item.emoji}</div>
              <div className="flex flex-1 flex-col">
                <span className="text-[16px] font-medium text-remine-dark">{item.title}</span>
                {item.value && <span className="text-[13px] text-remine-muted">{item.value}</span>}
              </div>
              <span className="text-remine-offline">›</span>
            </div>
          ))}
        </div>

        <div className="flex flex-col overflow-hidden rounded-[20px] bg-white">
          <p className="px-5 pb-2 pt-4 text-[12px] font-semibold tracking-wide text-remine-muted">앱 정보</p>
          {INFO_ITEMS.map((item, i) => (
            <div key={item.title} className={`flex items-center gap-3.5 px-5 py-3.5 ${i > 0 ? 'border-t border-remine-surfaceAlt' : ''}`}>
              <div className="flex size-11 items-center justify-center rounded-2xl bg-remine-surface text-[15px]">{item.emoji}</div>
              <span className="flex-1 text-[16px] font-medium text-remine-dark">{item.title}</span>
              {item.value && <span className="text-[14px] text-remine-muted">{item.value}</span>}
            </div>
          ))}
        </div>

        <button
          type="button"
          onClick={logout}
          className="h-[52px] rounded-2xl border border-remine-border bg-white text-[16px] font-semibold text-remine-danger"
        >
          로그아웃
        </button>
      </div>
    </Screen>
  )
}
