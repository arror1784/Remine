import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import Screen from '@/components/Screen'
import ModeBar from '@/components/ModeBar'
import BottomTabBar from '@/components/BottomTabBar'
import { getMemoryGallery, getMemoryStats, type MemoryPhoto, type MemoryStats } from '@/api/memory'
import { useAuthStore } from '@/store/auth'
import { COLORS } from '@/theme'

function formatYearMonth(iso: string) {
  const d = new Date(iso)
  return `${d.getFullYear()}년 ${d.getMonth() + 1}월`
}

function statusBadge(status: string) {
  return status === 'QUIZ_ACTIVE' ? '퀴즈 활용 중' : '대기 중'
}

export default function ChildMemoryGallery() {
  const location = useLocation()
  const childName = useAuthStore((s) => s.sessions.child?.name) ?? '자녀'
  const [photos, setPhotos] = useState<MemoryPhoto[]>([])
  const [stats, setStats] = useState<MemoryStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let active = true
    Promise.all([getMemoryGallery(), getMemoryStats()])
      .then(([gallery, memoryStats]) => {
        if (!active) return
        setPhotos(gallery)
        setStats(memoryStats)
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
    <Screen footer={<BottomTabBar role="child" accentColor={COLORS.blue} />}>
      <ModeBar label={`자녀 모드 — ${childName}님`} color={COLORS.blue} />

      <div className="flex items-center justify-between px-5 py-3.5">
        <h1 className="text-[22px] font-semibold text-remine-dark">추억 갤러리</h1>
        <Link
          to="/child/memories/add"
          state={{ backgroundLocation: location }}
          className="flex h-[38px] items-center justify-center rounded-full bg-remine-blue px-4 text-[15px] font-semibold text-white"
        >
          ＋ 사진 추가
        </Link>
      </div>

      <div className="flex flex-col gap-5 px-5 pb-10">
        <div className="flex gap-3 rounded-[20px] bg-remine-highlight px-5 py-4">
          <span className="text-[15px]">🧩</span>
          <div>
            <p className="text-[15px] font-semibold text-remine-dark">어머니 퀴즈에 활용돼요</p>
            <p className="pt-1 text-[13px] leading-[1.5] text-remine-subtle">
              자녀분들이 추가한 사진이 어머니의 추억 퀴즈에 자동으로 반영돼요. 소중한 추억을 추가해 보세요.
            </p>
          </div>
        </div>

        {loading && <p className="py-10 text-center text-[15px] text-remine-muted">불러오는 중...</p>}

        {!loading && failed && <p className="py-10 text-center text-[15px] text-remine-muted">불러오지 못했어요</p>}

        {!loading && !failed && (
          <>
            <div className="grid grid-cols-3 gap-2.5">
              <div className="flex flex-col items-center gap-1 rounded-[20px] border border-remine-border bg-white py-4">
                <span className="text-[22px] font-semibold text-remine-blue">{stats?.totalPhotos ?? 0}장</span>
                <span className="text-[13px] text-remine-muted">총 사진</span>
              </div>
              <div className="flex flex-col items-center gap-1 rounded-[20px] border border-remine-border bg-white py-4">
                <span className="text-[22px] font-semibold text-remine-pink">{stats?.quizActiveCount ?? 0}장</span>
                <span className="text-[13px] text-remine-muted">퀴즈 활용</span>
              </div>
              <div className="flex flex-col items-center gap-1 rounded-[20px] border border-remine-border bg-white py-4">
                <span className="text-[22px] font-semibold text-remine-orange">{stats?.addedThisMonth ?? 0}장</span>
                <span className="text-[13px] text-remine-muted">이번 달 추가</span>
              </div>
            </div>

            {photos.length === 0 ? (
              <p className="py-10 text-center text-[15px] text-remine-muted">아직 등록된 추억 사진이 없어요</p>
            ) : (
              <div className="flex flex-col gap-3">
                <h2 className="text-[18px] font-semibold text-remine-dark">추가된 사진</h2>
                {photos.map((p) => (
                  <div key={p.id} className="flex items-center gap-3.5 rounded-[20px] border border-remine-border bg-white p-3">
                    <img src={p.photoUrl} alt={p.title} className="size-[68px] shrink-0 rounded-xl object-cover" />
                    <div className="flex-1">
                      <p className="text-[15px] font-medium text-remine-dark">{p.title}</p>
                      <p className="text-[13px] text-remine-muted">{formatYearMonth(p.createdAt)}</p>
                    </div>
                    <span
                      className="shrink-0 rounded-full px-2.5 py-1 text-[12px] font-semibold"
                      style={{
                        backgroundColor: p.status === 'QUIZ_ACTIVE' ? COLORS.highlightBlue : COLORS.surface,
                        color: p.status === 'QUIZ_ACTIVE' ? COLORS.blue : COLORS.muted,
                      }}
                    >
                      {statusBadge(p.status)}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </>
        )}

        <Link
          to="/child/memories/add"
          state={{ backgroundLocation: location }}
          className="flex h-[52px] items-center justify-center gap-2 rounded-2xl bg-remine-dark text-[16px] font-semibold text-white"
        >
          📷 새 추억 사진 추가하기
        </Link>
      </div>
    </Screen>
  )
}
