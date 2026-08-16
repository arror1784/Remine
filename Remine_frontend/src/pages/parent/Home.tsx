import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import Screen from '@/components/Screen'
import BottomTabBar from '@/components/BottomTabBar'
import ModeBar from '@/components/ModeBar'
import { BellIcon } from '@/components/icons/NavIcons'
import { getUnreadCount } from '@/api/notification'
import familyPhoto from '@/assets/memories/family-trip.png'
import { getRecommendation, getTodaySummary, type ActivityActionType, type ActivityRecommendation, type TodaySummary } from '@/api/activity'
import { getTodayQuiz, type TodayQuiz } from '@/api/memory'
import { COLORS } from '@/theme'

const ACTION_CTA: Record<Exclude<ActivityActionType, 'NONE'>, { label: string; to: string }> = {
  WALK: { label: '산책 시작하기', to: '/parent/reminders/walk' },
  CALL: { label: '전화 연결하기', to: '/parent/reminders/call' },
  QUIZ: { label: '퀴즈 시작하기', to: '/parent/reminders/quiz' },
}

const WEEK_PATTERN = [
  { day: '수', height: 24 },
  { day: '목', height: 27 },
  { day: '금', height: 20 },
  { day: '토', height: 29 },
  { day: '일', height: 15 },
  { day: '월', height: 32 },
  { day: '오늘', height: 27, today: true },
]

const FALLBACK_ACTIVITIES = [
  { emoji: '🌙', label: '수면', value: '7시간 12분', percent: 90, barColor: COLORS.blue, note: '평소와 비슷', noteColor: COLORS.teal },
  { emoji: '👟', label: '걸음', value: '4,280보', percent: 55, barColor: COLORS.orange, note: '조금 적어요', noteColor: COLORS.gold },
  { emoji: '🌿', label: '외출', value: '1회', percent: 40, barColor: COLORS.orange, note: '평소보다 적음', noteColor: COLORS.gold },
  { emoji: '💬', label: '대화', value: '0회', percent: 10, barColor: COLORS.borderMuted, note: '오늘은 휴식', noteColor: COLORS.muted },
]

function formatSleep(minutes: number) {
  return `${Math.floor(minutes / 60)}시간 ${minutes % 60}분`
}

// percent buckets are a placeholder — same "not vs. others, vs. your own goal" spirit as before,
// just driven by real data now instead of fixed mock numbers.
function bucket(percent: number, zeroValue: boolean) {
  if (zeroValue) return { note: '오늘은 휴식', noteColor: COLORS.muted, barColor: COLORS.borderMuted }
  if (percent >= 70) return { note: '평소와 비슷', noteColor: COLORS.teal, barColor: COLORS.blue }
  return { note: '평소보다 적어요', noteColor: COLORS.gold, barColor: COLORS.orange }
}

function buildActivities(summary: TodaySummary | null) {
  if (!summary?.stat) return FALLBACK_ACTIVITIES
  const { stat, sleepPercent, stepsPercent, outingPercent, socialPercent } = summary
  return [
    { emoji: '🌙', label: '수면', value: formatSleep(stat.sleepMinutes), percent: sleepPercent, ...bucket(sleepPercent, stat.sleepMinutes === 0) },
    { emoji: '👟', label: '걸음', value: `${stat.steps.toLocaleString()}보`, percent: stepsPercent, ...bucket(stepsPercent, stat.steps === 0) },
    { emoji: '🌿', label: '외출', value: `${stat.outingCount}회`, percent: outingPercent, ...bucket(outingPercent, stat.outingCount === 0) },
    { emoji: '💬', label: '대화', value: `${stat.socialContactCount}회`, percent: socialPercent, ...bucket(socialPercent, stat.socialContactCount === 0) },
  ]
}

export default function ParentHome() {
  const location = useLocation()
  const [unreadCount, setUnreadCount] = useState(0)
  const [recommendation, setRecommendation] = useState<ActivityRecommendation | null>(null)
  const [summary, setSummary] = useState<TodaySummary | null>(null)
  const [todayQuiz, setTodayQuiz] = useState<TodayQuiz | null>(null)

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
    getTodayQuiz()
      .then((data) => {
        if (active) setTodayQuiz(data)
      })
      .catch(() => {})
    getUnreadCount()
      .then((count) => {
        if (active) setUnreadCount(count)
      })
      .catch(() => {})
    return () => {
      active = false
    }
  }, [])

  // Until the fetch lands (or if it fails) the card keeps its static copy so it never flashes empty.
  const recommendationMessage = recommendation?.parentMessage ?? '오늘 오후 산책 어떠세요?'
  const cta = recommendation ? (recommendation.actionType === 'NONE' ? null : ACTION_CTA[recommendation.actionType]) : ACTION_CTA.WALK
  const activities = buildActivities(summary)

  return (
    <Screen footer={<BottomTabBar role="parent" accentColor={COLORS.pink} />}>
      <ModeBar label="부모님 모드 — 윤정아님" color={COLORS.pink} />

      <div className="flex items-center justify-between px-5 py-3.5">
        <span className="text-[19px] font-semibold text-remine-dark">
          Rem<span className="text-remine-pink">e</span>ine
        </span>
        <div className="flex items-center gap-2">
          <Link to="/parent/notifications" state={{ backgroundLocation: location }} className="relative flex items-start p-1">
            <BellIcon className="size-6 text-remine-dark" />
            {unreadCount > 0 && (
              <span className="absolute right-[6.75px] top-[4.7px] size-2 rounded-full border-2 border-remine-bg bg-remine-pink" />
            )}
          </Link>
          <Link to="/parent/mypage" className="flex size-9 items-center justify-center rounded-full bg-remine-highlight text-[14px]">
            👩
          </Link>
        </div>
      </div>

      <div className="flex flex-col gap-7 px-5 pb-8">
        <div className="flex flex-col gap-1">
          <p className="text-[16px] text-remine-muted">2026년 8월 11일 화요일</p>
          <h1 className="text-[26px] font-semibold leading-[1.3] text-remine-dark">
            좋은 아침이에요,
            <br />
            윤정아님 👋
          </h1>
        </div>

        <div className="relative overflow-hidden rounded-3xl bg-remine-surfaceDark px-6 py-7">
          <div
            aria-hidden
            className="absolute -right-8 -top-10 size-[130px] rounded-full bg-remine-pink opacity-10 blur-[20px]"
          />
          <div aria-hidden className="absolute -bottom-8 left-5 size-[100px] rounded-full bg-remine-blue opacity-10 blur-[16px]" />
          <p className="text-[13px] tracking-wide text-white/40">오늘의 인지 건강</p>
          <p className="pb-1 pt-1 text-[26px] font-semibold leading-[1.3] text-white">평소와 비슷한 하루예요 🌿</p>
          <p className="pb-4 text-[15px] text-white/50">수면과 활동이 안정적이에요.</p>
          <div className="flex flex-col gap-3 border-t border-white/8 pt-5">
            <p className="text-[12px] tracking-wide text-white">최근 7일 생활 패턴</p>
            <div className="flex items-end justify-center gap-[5px]">
              {WEEK_PATTERN.map(({ day, height, today }) => (
                <div key={day} className="flex w-10 flex-col items-center gap-1.5">
                  <div
                    className={`w-full rounded ${today ? 'bg-remine-pink' : 'bg-remine-blue/20'}`}
                    style={{
                      height: `${height}px`,
                      alignSelf: 'flex-end',
                    }}
                  />
                  <span className={`text-[10px] ${today ? 'text-remine-pink' : 'text-white/30'}`}>
                    {day}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="flex flex-col">
          <h2 className="pb-1 text-[20px] font-semibold text-remine-dark">오늘의 활동</h2>
          {activities.map((a, i) => (
            <div
              key={a.label}
              className={`flex items-center gap-4 py-[18px] ${i < activities.length - 1 ? 'border-b border-remine-border' : ''}`}
            >
              <div className="flex size-12 shrink-0 items-center justify-center rounded-2xl bg-remine-surface text-[17px]">{a.emoji}</div>
              <div className="flex flex-1 flex-col gap-2">
                <div className="flex items-center justify-between">
                  <span className="text-[16px] text-remine-dark">{a.label}</span>
                  <span className="text-[18px] text-remine-dark">{a.value}</span>
                </div>
                <div className="flex items-center gap-2.5">
                  <div className="h-1.5 flex-1 overflow-hidden rounded-full bg-remine-border">
                    <div className="h-full rounded-full" style={{ width: `${a.percent}%`, backgroundColor: a.barColor }} />
                  </div>
                  <span className="whitespace-nowrap text-[13px]" style={{ color: a.noteColor }}>
                    {a.note}
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>

        <div className="flex flex-col gap-2 rounded-[20px] bg-remine-highlight px-5 pb-5 pt-6">
          <div className="flex items-center gap-2">
            <span className="size-2 rounded-sm bg-remine-pink" />
            <span className="text-[13px] font-semibold tracking-wide text-remine-dark">AI 추천</span>
          </div>
          <p className={`pt-1 text-[20px] font-semibold leading-[1.4] text-remine-dark ${cta ? 'pb-3' : ''}`}>
            {recommendationMessage}
          </p>
          {cta && (
            <Link
              to={cta.to}
              state={{ backgroundLocation: location }}
              className="flex h-[52px] items-center justify-center rounded-2xl bg-remine-dark text-[17px] font-semibold text-white"
            >
              {cta.label}
            </Link>
          )}
        </div>

        <div className="flex flex-col gap-4">
          <h2 className="text-[20px] font-semibold text-remine-dark">오늘의 추억</h2>
          <div className="overflow-hidden rounded-[20px] border border-remine-border bg-white">
            {todayQuiz && !todayQuiz.photo ? (
              <p className="px-5 py-8 text-center text-[15px] text-remine-subtle">오늘은 준비된 추억 퀴즈가 없어요</p>
            ) : (
              <>
                <div className="h-[164px] w-full overflow-hidden bg-remine-highlight">
                  <img
                    src={todayQuiz?.photo?.photoUrl ?? familyPhoto}
                    alt={todayQuiz?.photo?.title ?? '가족 나들이 사진'}
                    className="size-full object-cover"
                  />
                </div>
                <div className="flex flex-col gap-1.5 px-5 pb-5 pt-[18px]">
                  <span className="w-fit rounded-full bg-remine-highlight px-2.5 py-0.5 text-[12px] text-remine-dark">
                    {todayQuiz?.photo?.memoryLabel ?? '2022년 봄'}
                  </span>
                  <p className="pt-1 text-[17px] text-remine-dark">{todayQuiz?.photo ? `${todayQuiz.photo.title} 📸` : '가족 여행 📸'}</p>
                  <p className="pb-2 text-[15px] text-remine-subtle">이 사진과 관련된 퀴즈를 풀어볼까요?</p>
                  <Link
                    to="/parent/memories/quiz"
                    className="flex h-12 items-center justify-center rounded-xl bg-remine-highlight text-[16px] font-semibold text-remine-dark"
                  >
                    추억 퀴즈 풀기
                  </Link>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </Screen>
  )
}
