import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import Screen from '@/components/Screen'
import ModeBar from '@/components/ModeBar'
import BottomTabBar from '@/components/BottomTabBar'
import { getRecommendation, getTodaySummary, type ActivityRecommendation, type TodaySummary } from '@/api/activity'
import { COLORS } from '@/theme'

const SUGGESTIONS = [
  { emoji: '🏃', dot: COLORS.dark, title: '20분 산책', desc: '오후 2~4시 사이 가볍게', to: '/parent/reminders/walk' },
  { emoji: '📞', dot: COLORS.pink, title: '가족에게 전화하기', desc: '짧은 통화도 큰 힘이 돼요', to: '/parent/reminders/call' },
  { emoji: '🧩', dot: COLORS.orange, title: '오늘의 추억 퀴즈', desc: '5분이면 충분해요', to: '/parent/reminders/quiz' },
]

const FALLBACK_SUMMARY = [
  { metric: 'sleep', emoji: '🌙', label: '수면', value: '7시간 12분', percent: 90, barColor: COLORS.blue, tag: '안정', tagBg: COLORS.highlightBlue, tagColor: COLORS.teal },
  { metric: 'steps', emoji: '🚶', label: '활동량', value: '4,280보', percent: 55, barColor: COLORS.orange, tag: '조금 적음', tagBg: COLORS.highlightOrange, tagColor: COLORS.gold },
  { metric: 'outing', emoji: '🌿', label: '외출', value: '1회', percent: 40, barColor: COLORS.orange, tag: '조금 적음', tagBg: COLORS.highlightOrange, tagColor: COLORS.gold },
  { metric: 'social', emoji: '💬', label: '사회 활동', value: '연락 없음', percent: 10, barColor: COLORS.borderMuted, tag: '오늘은 휴식', tagBg: COLORS.surface, tagColor: COLORS.muted },
]

function formatSleep(minutes: number) {
  return `${Math.floor(minutes / 60)}시간 ${minutes % 60}분`
}

// Same "vs. your own baseline" bucketing Home.tsx uses, just also carrying the
// tag copy this screen's pattern-analysis rows show alongside the bar.
function bucket(percent: number, zeroValue: boolean) {
  if (zeroValue) return { tag: '오늘은 휴식', tagBg: COLORS.surface, tagColor: COLORS.muted, barColor: COLORS.borderMuted }
  if (percent >= 70) return { tag: '안정', tagBg: COLORS.highlightBlue, tagColor: COLORS.teal, barColor: COLORS.blue }
  return { tag: '조금 적음', tagBg: COLORS.highlightOrange, tagColor: COLORS.gold, barColor: COLORS.orange }
}

function buildSummary(summary: TodaySummary | null) {
  if (!summary?.stat) return FALLBACK_SUMMARY
  const { stat, sleepPercent, stepsPercent, outingPercent, socialPercent } = summary
  return [
    { metric: 'sleep', emoji: '🌙', label: '수면', value: formatSleep(stat.sleepMinutes), percent: sleepPercent, ...bucket(sleepPercent, stat.sleepMinutes === 0) },
    { metric: 'steps', emoji: '🚶', label: '활동량', value: `${stat.steps.toLocaleString()}보`, percent: stepsPercent, ...bucket(stepsPercent, stat.steps === 0) },
    { metric: 'outing', emoji: '🌿', label: '외출', value: `${stat.outingCount}회`, percent: outingPercent, ...bucket(outingPercent, stat.outingCount === 0) },
    { metric: 'social', emoji: '💬', label: '사회 활동', value: stat.socialContactCount === 0 ? '연락 없음' : `${stat.socialContactCount}회`, percent: socialPercent, ...bucket(socialPercent, stat.socialContactCount === 0) },
  ]
}

export default function ParentToday() {
  const location = useLocation()
  const [recommendation, setRecommendation] = useState<ActivityRecommendation | null>(null)
  const [summary, setSummary] = useState<TodaySummary | null>(null)

  useEffect(() => {
    let active = true
    getRecommendation()
      .then((data) => {
        if (active) setRecommendation(data)
      })
      .catch(() => {})
    getTodaySummary()
      .then((data) => {
        if (active) setSummary(data)
      })
      .catch(() => {})
    return () => {
      active = false
    }
  }, [])

  // Until the fetch lands (or if it fails) the card keeps its static copy so it never flashes empty.
  const headline = recommendation?.parentMessage ?? '전반적으로 안정된 하루예요 🙂'
  const rows = buildSummary(summary)

  return (
    <Screen footer={<BottomTabBar role="parent" accentColor={COLORS.pink} />}>
      <ModeBar label="부모님 모드 — 윤정아님" color={COLORS.pink} />

      <div className="flex items-center gap-2 px-5 py-3.5">
        <h1 className="text-[22px] font-semibold text-remine-dark">오늘의 분석</h1>
        <span className="flex items-center gap-1.5 rounded-full bg-remine-surface px-3 py-1.5 text-[13px] font-semibold text-remine-dark">
          <span className="size-1.5 rounded-full bg-remine-blue" />
          AI 분석 완료
        </span>
      </div>

      <div className="flex flex-col gap-7 px-5 pb-8">
        <div className="flex flex-col gap-1">
          <p className="text-[16px] text-remine-muted">2026년 8월 11일 화요일</p>
          <h2 className="text-[23px] font-semibold text-remine-dark">{headline}</h2>
          <p className="pt-1 text-[16px] leading-[1.5] text-remine-subtle">수면과 활동이 평소와 비슷해요. 활동량이 조금 적으니 오후에 가볍게 움직여 보세요.</p>
        </div>

        <div data-testid="today-summary" className="flex flex-col gap-4 rounded-[20px] border border-remine-border bg-white p-5">
          {rows.map((s, i) => (
            <div key={s.label} data-testid={`today-summary-${s.metric}`} className={`flex gap-3.5 ${i < rows.length - 1 ? 'border-b border-remine-surfaceSoft2 pb-4' : ''}`}>
              <span className="w-[26px] shrink-0 text-center text-[15px]">{s.emoji}</span>
              <div className="flex flex-1 flex-col gap-2">
                <div className="flex items-center justify-between">
                  <span className="text-[16px] text-remine-dark">{s.label}</span>
                  <span data-testid="value" className="text-[16px] text-remine-dark">{s.value}</span>
                </div>
                <div className="h-2 overflow-hidden rounded-full bg-remine-surfaceAlt">
                  <div data-testid="bar" className="h-full rounded-full" style={{ width: `${s.percent}%`, backgroundColor: s.barColor }} />
                </div>
              </div>
            </div>
          ))}
        </div>

        <div className="flex flex-col">
          <h2 className="pb-1 text-[20px] font-semibold text-remine-dark">생활 패턴 분석</h2>
          {rows.map((p, i) => (
            <div key={p.label} data-testid={`today-pattern-${p.metric}`} className={`flex items-center gap-3.5 py-[18px] ${i < rows.length - 1 ? 'border-b border-remine-border' : ''}`}>
              <div className="flex size-12 shrink-0 items-center justify-center rounded-2xl bg-remine-surface text-[17px]">{p.emoji}</div>
              <div className="flex flex-1 flex-col gap-1">
                <div className="flex items-center gap-2">
                  <span className="text-[17px] text-remine-dark">{p.label}</span>
                  <span className="rounded-full px-2.5 py-0.5 text-[12px]" style={{ backgroundColor: p.tagBg, color: p.tagColor }}>
                    {p.tag}
                  </span>
                </div>
                <span className="text-[15px] text-remine-muted">{p.value}</span>
              </div>
              <span className="text-remine-offline">›</span>
            </div>
          ))}
        </div>

        <div className="flex flex-col gap-1">
          <div className="flex items-center gap-2 pb-1">
            <span className="size-2 rounded-sm bg-remine-pink" />
            <h2 className="text-[20px] font-semibold text-remine-dark">오늘 해볼 수 있는 활동</h2>
          </div>
          {SUGGESTIONS.map((s, i) => (
            <div key={s.title} className={`flex items-center gap-4 py-[18px] ${i < SUGGESTIONS.length - 1 ? 'border-b border-remine-border' : ''}`}>
              <div className="flex size-[52px] shrink-0 items-center justify-center rounded-2xl bg-remine-surface text-[18px]">{s.emoji}</div>
              <div className="flex flex-1 flex-col gap-1">
                <div className="flex items-center gap-2">
                  <span className="size-1.5 rounded-full" style={{ backgroundColor: s.dot }} />
                  <span className="text-[17px] text-remine-dark">{s.title}</span>
                </div>
                <span className="text-[14px] text-remine-muted">{s.desc}</span>
              </div>
              <Link
                to={s.to}
                state={{ backgroundLocation: location }}
                className="flex h-10 shrink-0 items-center justify-center rounded-xl bg-remine-highlight px-4 text-[15px] font-semibold text-remine-dark"
              >
                시작
              </Link>
            </div>
          ))}
        </div>
      </div>
    </Screen>
  )
}
