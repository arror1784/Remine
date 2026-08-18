import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import Screen from '@/components/Screen'
import ModeBar from '@/components/ModeBar'
import BottomTabBar from '@/components/BottomTabBar'
import {
  getChecklist,
  getTimeline,
  getTodaySummary,
  type ChecklistItem,
  type ChecklistItemType,
  type TimelineEvent,
  type TodaySummary,
} from '@/api/activity'
import { COLORS } from '@/theme'

const ITEM_META: Record<ChecklistItemType, { emoji: string; label: string; bg: string; cheerType: string | null }> = {
  SLEEP: { emoji: '🌙', label: '수면', bg: COLORS.highlightBlue, cheerType: 'sleep' },
  BREAKFAST: { emoji: '🍚', label: '아침식사', bg: COLORS.surfaceSoft, cheerType: 'breakfast' },
  WALK: { emoji: '🚶', label: '산책', bg: COLORS.surfaceSoft, cheerType: 'walk' },
  QUIZ: { emoji: '🧩', label: '퀴즈', bg: COLORS.surfaceSoft, cheerType: 'quiz' },
}

function clockTime(iso: string) {
  return new Date(iso).toLocaleTimeString('ko-KR', { hour: 'numeric', minute: '2-digit' })
}

function describeItem(item: ChecklistItem, summary: TodaySummary | null) {
  if (item.done) return item.completedAt ? `${clockTime(item.completedAt)} 완료` : '완료'
  if (item.type === 'WALK' && summary) return `목표의 ${summary.stepsPercent}%`
  if (item.type === 'QUIZ') return '오늘 퀴즈 대기 중'
  return '아직 기록 없어요'
}

export default function ChildToday() {
  const location = useLocation()
  const navigate = useNavigate()
  const [cheeredIds, setCheeredIds] = useState<string[]>([])

  const [checklist, setChecklist] = useState<ChecklistItem[]>([])
  const [timeline, setTimeline] = useState<TimelineEvent[]>([])
  const [summary, setSummary] = useState<TodaySummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let active = true
    Promise.all([getChecklist(), getTimeline(), getTodaySummary()])
      .then(([checklistItems, timelineEvents, todaySummary]) => {
        if (!active) return
        setChecklist(checklistItems)
        setTimeline(timelineEvents)
        setSummary(todaySummary)
      })
      .catch(() => {
        if (active) setFailed(true)
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    const cheeredItemId = (location.state as { cheeredItemId?: string } | null)?.cheeredItemId
    if (!cheeredItemId) return
    setCheeredIds((prev) => (prev.includes(cheeredItemId) ? prev : [...prev, cheeredItemId]))
    navigate(location.pathname, { replace: true, state: {} })
    // Only ever react to a freshly-arrived cheeredItemId from CheerMessage's navigate() —
    // location.pathname/navigate are stable here and re-running on them would loop.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location.state])

  const doneCount = checklist.filter((item) => item.done).length
  const totalCount = checklist.length
  const remainingCount = totalCount - doneCount
  const percent = totalCount > 0 ? Math.round((doneCount / totalCount) * 100) : 0

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

      {loading && <p className="py-10 text-center text-[15px] text-remine-muted">불러오는 중...</p>}
      {!loading && failed && <p className="py-10 text-center text-[15px] text-remine-muted">불러오지 못했어요</p>}

      {!loading && !failed && (
        <div className="flex flex-col gap-7 px-5 pb-8">
          <div className="relative overflow-hidden rounded-3xl bg-remine-surfaceDark px-6 py-7">
            <div aria-hidden className="absolute -right-5 -top-7 size-[120px] rounded-full bg-remine-blue opacity-10 blur-[18px]" />
            <p className="text-[13px] tracking-wide text-white/40">오늘의 활동 달성</p>
            <p className="pb-1 pt-1 text-[24px] font-semibold leading-[1.3] text-white">
              오늘 {doneCount}/{totalCount} 완료됐어요 🌿
            </p>
            <div className="my-3 h-2 overflow-hidden rounded-full bg-white/15">
              <div className="h-full rounded-full bg-remine-blue" style={{ width: `${percent}%` }} />
            </div>
            <p className="text-[14px] text-white/50">
              {remainingCount > 0 ? `${remainingCount}가지 활동이 아직 남았어요.` : '오늘 활동을 모두 마쳤어요!'}
            </p>
          </div>

          <div className="flex flex-col gap-2.5">
            <h2 className="pb-1 text-[20px] font-semibold text-remine-dark">활동 체크리스트</h2>
            {checklist.map((item) => {
              const meta = ITEM_META[item.type]
              const cheered = cheeredIds.includes(item.type.toLowerCase())
              return (
                <div key={item.id} className="flex items-center gap-3.5 rounded-[20px] border border-remine-border bg-white px-5 py-4">
                  <div className="flex size-12 shrink-0 items-center justify-center rounded-2xl text-[17px]" style={{ backgroundColor: meta.bg }}>
                    {meta.emoji}
                  </div>
                  <div className="flex flex-1 flex-col gap-0.5">
                    <div className="flex items-center gap-2">
                      <span className="text-[16px] font-medium text-remine-dark">{meta.label}</span>
                      <span
                        className="rounded-full px-2 py-0.5 text-[12px] font-semibold"
                        style={{ backgroundColor: item.done ? COLORS.highlightBlue : COLORS.surface, color: item.done ? COLORS.blue : COLORS.muted }}
                      >
                        {item.done ? '완료' : '미완료'}
                      </span>
                    </div>
                    <span className="text-[13px] text-remine-muted">{describeItem(item, summary)}</span>
                  </div>
                  {item.done ? (
                    <span className="flex size-7 items-center justify-center rounded-full bg-remine-blue text-white">✓</span>
                  ) : !meta.cheerType ? null : cheered ? (
                    <span
                      className="flex h-[34px] shrink-0 items-center justify-center rounded-xl px-3.5 text-[13px] font-semibold text-remine-muted"
                      style={{ backgroundColor: COLORS.surface }}
                    >
                      보냈어요 ✓
                    </span>
                  ) : (
                    <Link
                      to={`/child/today/cheer/${meta.cheerType}`}
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
            {timeline.length === 0 ? (
              <p className="rounded-[20px] border border-remine-border bg-white px-5 py-8 text-center text-[14px] text-remine-muted">
                아직 기록된 활동이 없어요.
              </p>
            ) : (
              <div className="flex flex-col gap-[18px] rounded-[20px] border border-remine-border bg-white px-5 py-[18px]">
                {timeline.map((t, i) => (
                  <div key={t.id} className="relative flex gap-4">
                    <div className="flex w-3 flex-col items-center pt-1">
                      <span className="size-3 shrink-0 rounded-full" style={{ backgroundColor: t.colorHint ?? COLORS.blue }} />
                      {i < timeline.length - 1 && <span className="mt-1 h-8 w-0.5 bg-remine-surfaceAlt" />}
                    </div>
                    <div className="flex flex-col gap-0.5">
                      <span className="text-[12px] text-remine-muted">{clockTime(t.occurredAt)}</span>
                      <span className="text-[14px] font-medium text-remine-dark">{t.label}</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </Screen>
  )
}
