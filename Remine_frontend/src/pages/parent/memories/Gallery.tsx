import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import Screen from '@/components/Screen'
import ModeBar from '@/components/ModeBar'
import BottomTabBar from '@/components/BottomTabBar'
import { getMemoryGallery, getTodayQuiz, type MemoryPhoto, type TodayQuiz } from '@/api/memory'
import { COLORS } from '@/theme'

function formatYearMonth(iso: string) {
  const d = new Date(iso)
  return `${d.getFullYear()}년 ${d.getMonth() + 1}월`
}

export default function MemoryGallery() {
  const [photos, setPhotos] = useState<MemoryPhoto[]>([])
  const [todayQuiz, setTodayQuiz] = useState<TodayQuiz | null>(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let active = true
    Promise.all([getMemoryGallery(), getTodayQuiz()])
      .then(([gallery, quiz]) => {
        if (!active) return
        setPhotos(gallery)
        setTodayQuiz(quiz)
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

  return (
    <Screen footer={<BottomTabBar role="parent" accentColor={COLORS.pink} />}>
      <ModeBar label="부모님 모드 — 윤정아님" color={COLORS.pink} />

      <div className="flex items-center justify-between px-5 py-3.5">
        <h1 className="text-[22px] font-semibold text-remine-dark">추억 갤러리</h1>
        <button type="button" className="flex h-[38px] items-center justify-center rounded-full bg-remine-pink px-4 text-[15px] font-semibold text-white">
          ＋ 사진 추가
        </button>
      </div>

      <div className="flex flex-col gap-4 px-5 pb-10">
        {loading && <p className="py-10 text-center text-[15px] text-remine-muted">불러오는 중...</p>}

        {!loading && failed && <p className="py-10 text-center text-[15px] text-remine-muted">불러오지 못했어요</p>}

        {!loading && !failed && todayQuiz?.photo && (
          <Link
            to="/parent/memories/quiz"
            className="relative flex items-center gap-3.5 overflow-hidden rounded-[20px] bg-remine-surfaceDark px-5 py-4"
          >
            <div aria-hidden className="absolute -right-5 -top-5 size-20 rounded-full bg-remine-pink opacity-15 blur-[12px]" />
            <div className="flex size-[52px] shrink-0 items-center justify-center rounded-2xl bg-remine-highlight text-xl">🧩</div>
            <div className="flex-1">
              <p className="text-[16.9px] font-semibold leading-[1.3] text-white">
                오늘의 추억 퀴즈
                <br />
                준비됐어요
              </p>
              <p className="pt-0.5 text-[14px] text-white/50">{todayQuiz.photo.title} 사진으로</p>
            </div>
            <span className="shrink-0 rounded-xl bg-remine-pink px-4 py-2.5 text-[15px] font-semibold text-white">풀기</span>
          </Link>
        )}

        {!loading && !failed && photos.length === 0 && (
          <p className="py-10 text-center text-[15px] text-remine-muted">아직 등록된 추억 사진이 없어요</p>
        )}

        {!loading && !failed && photos.length > 0 && (
          <div className="grid grid-cols-2 gap-3">
            {photos.map((m) => {
              const quizReady = m.status === 'QUIZ_ACTIVE'
              const content = (
                <>
                  <div className="h-[130px] w-full overflow-hidden">
                    <img src={m.photoUrl} alt={m.title} className="size-full object-cover" />
                  </div>
                  <div className="flex flex-col gap-1 px-3 pb-3.5 pt-2.5">
                    <p data-testid="memory-title" className="text-[14px] text-remine-dark">{m.title}</p>
                    <p data-testid="memory-date" className="text-[13px] text-remine-muted">{formatYearMonth(m.createdAt)}</p>
                  </div>
                  {m.attempted && (
                    <span className="absolute right-2 top-2 flex size-[26px] items-center justify-center rounded-full bg-remine-pink text-white">
                      ✓
                    </span>
                  )}
                </>
              )
              return quizReady ? (
                <Link
                  key={m.id}
                  to={`/parent/memories/${m.id}/quiz`}
                  data-testid="memory-card"
                  className="relative overflow-hidden rounded-[20px] bg-remine-surface"
                >
                  {content}
                </Link>
              ) : (
                <div key={m.id} data-testid="memory-card" className="relative overflow-hidden rounded-[20px] bg-remine-surface">
                  {content}
                </div>
              )
            })}
          </div>
        )}

        <button
          type="button"
          className="flex h-[52px] items-center justify-center gap-2 rounded-2xl bg-remine-dark text-[16px] font-semibold text-white"
        >
          📷 새 추억 사진 추가하기
        </button>
      </div>
    </Screen>
  )
}
