import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import Screen from '@/components/Screen'
import ModeBar from '@/components/ModeBar'
import BottomTabBar from '@/components/BottomTabBar'
import { COLORS } from '@/theme'

const CHECKLIST = [
  { id: 'sleep', emoji: '🌙', label: '수면', status: '완료', done: true, desc: '오전 7:14 기상', bg: COLORS.highlightBlue },
  { id: 'breakfast', emoji: '🍚', label: '아침식사', status: '미완료', done: false, desc: '아직 기록 없어요', bg: COLORS.surfaceSoft },
  { id: 'walk', emoji: '🚶', label: '산책', status: '미완료', done: false, desc: '목표의 55%', bg: COLORS.surfaceSoft },
  { id: 'quiz', emoji: '🧩', label: '퀴즈', status: '미완료', done: false, desc: '오늘 퀴즈 대기 중', bg: COLORS.surfaceSoft },
]

const TIMELINE = [
  { time: '오전 7:14', label: '기상 확인됨', color: COLORS.blue },
  { time: '오전 9:02', label: '앱 열어보심', color: COLORS.blue },
  { time: '오전 10:30', label: '산책 시작 (4,280보)', color: COLORS.orange },
  { time: '오후 12:00', label: '이후 활동 없음', color: COLORS.borderMuted, faint: true },
]

export default function ChildToday() {
  const location = useLocation()
  const navigate = useNavigate()
  const [cheeredIds, setCheeredIds] = useState<string[]>([])

  useEffect(() => {
    const cheeredItemId = (location.state as { cheeredItemId?: string } | null)?.cheeredItemId
    if (!cheeredItemId) return
    setCheeredIds((prev) => (prev.includes(cheeredItemId) ? prev : [...prev, cheeredItemId]))
    navigate(location.pathname, { replace: true, state: {} })
    // Only ever react to a freshly-arrived cheeredItemId from CheerMessage's navigate() —
    // location.pathname/navigate are stable here and re-running on them would loop.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location.state])

  return (
    <Screen footer={<BottomTabBar role="child" accentColor={COLORS.blue} />}>
      <ModeBar label="자녀 모드 — 지영님" color={COLORS.blue} />

      <div className="flex items-center gap-2 px-5 py-3.5">
        <h1 className="text-[22px] font-semibold text-remine-dark">어머니의 오늘</h1>
        <span className="flex items-center gap-1.5 rounded-full bg-remine-surface px-3 py-1.5 text-[13px] font-semibold text-remine-dark">
          <span className="size-1.5 rounded-full bg-remine-blue" />
          실시간 모니터링
        </span>
      </div>

      <div className="flex flex-col gap-7 px-5 pb-8">
        <div className="relative overflow-hidden rounded-3xl bg-remine-surfaceDark px-6 py-7">
          <div aria-hidden className="absolute -right-5 -top-7 size-[120px] rounded-full bg-remine-blue opacity-10 blur-[18px]" />
          <p className="text-[13px] tracking-wide text-white/40">오늘의 활동 달성</p>
          <p className="pb-1 pt-1 text-[24px] font-semibold leading-[1.3] text-white">오늘 1/4 완료됐어요 🌿</p>
          <div className="my-3 h-2 overflow-hidden rounded-full bg-white/15">
            <div className="h-full w-1/4 rounded-full bg-remine-blue" />
          </div>
          <p className="text-[14px] text-white/50">3가지 활동이 아직 남았어요.</p>
        </div>

        <div className="flex flex-col gap-2.5">
          <h2 className="pb-1 text-[20px] font-semibold text-remine-dark">활동 체크리스트</h2>
          {CHECKLIST.map((item) => {
            const cheered = cheeredIds.includes(item.id)
            return (
              <div key={item.id} className="flex items-center gap-3.5 rounded-[20px] border border-remine-border bg-white px-5 py-4">
                <div className="flex size-12 shrink-0 items-center justify-center rounded-2xl text-[17px]" style={{ backgroundColor: item.bg }}>
                  {item.emoji}
                </div>
                <div className="flex flex-1 flex-col gap-0.5">
                  <div className="flex items-center gap-2">
                    <span className="text-[16px] font-medium text-remine-dark">{item.label}</span>
                    <span
                      className="rounded-full px-2 py-0.5 text-[12px] font-semibold"
                      style={{ backgroundColor: item.done ? COLORS.highlightBlue : COLORS.surface, color: item.done ? COLORS.blue : COLORS.muted }}
                    >
                      {item.status}
                    </span>
                  </div>
                  <span className="text-[13px] text-remine-muted">{item.desc}</span>
                </div>
                {item.done ? (
                  <span className="flex size-7 items-center justify-center rounded-full bg-remine-blue text-white">✓</span>
                ) : cheered ? (
                  <span
                    className="flex h-[34px] shrink-0 items-center justify-center rounded-xl px-3.5 text-[13px] font-semibold text-remine-muted"
                    style={{ backgroundColor: COLORS.surface }}
                  >
                    보냈어요 ✓
                  </span>
                ) : (
                  <Link
                    to={`/child/today/cheer/${item.id}`}
                    state={{ backgroundLocation: location }}
                    className="flex h-[34px] shrink-0 items-center justify-center rounded-xl px-3.5 text-[13px] font-semibold text-remine-dark"
                    style={{ backgroundColor: COLORS.highlight }}
                  >
                    응원 보내기
                  </Link>
                )}
              </div>
            )
          })}
        </div>

        <div className="flex flex-col gap-3">
          <h2 className="text-[20px] font-semibold text-remine-dark">오늘의 타임라인</h2>
          <div className="flex flex-col gap-[18px] rounded-[20px] border border-remine-border bg-white px-5 py-[18px]">
            {TIMELINE.map((t, i) => (
              <div key={t.time} className="relative flex gap-4">
                <div className="flex w-3 flex-col items-center pt-1">
                  <span className="size-3 shrink-0 rounded-full" style={{ backgroundColor: t.color }} />
                  {i < TIMELINE.length - 1 && <span className="mt-1 h-8 w-0.5 bg-remine-surfaceAlt" />}
                </div>
                <div className="flex flex-col gap-0.5">
                  <span className="text-[12px] text-remine-muted">{t.time}</span>
                  <span className="text-[14px] font-medium" style={{ color: t.faint ? COLORS.borderSoft : COLORS.dark }}>
                    {t.label}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </Screen>
  )
}
