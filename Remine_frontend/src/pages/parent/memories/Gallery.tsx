import { Link } from 'react-router-dom'
import Screen from '@/components/Screen'
import ModeBar from '@/components/ModeBar'
import BottomTabBar from '@/components/BottomTabBar'
import familyTrip from '@/assets/memories/family-trip.png'
import grandchild from '@/assets/memories/grandchild.png'
import sokchoTrip from '@/assets/memories/sokcho-trip.png'
import anniversary from '@/assets/memories/anniversary.jpg'
import { COLORS } from '@/theme'

const MEMORIES = [
  { photo: familyTrip, title: '2022년 봄 가족 여행', date: '2022년 5월', quizzed: true },
  { photo: grandchild, title: '첫 손주 돌잔치', date: '2024년 2월', quizzed: false },
  { photo: sokchoTrip, title: '속초 여행', date: '2024년 10월', quizzed: false },
  { photo: anniversary, title: '결혼 40주년 기념일', date: '2026년 6월', quizzed: true },
]

export default function MemoryGallery() {
  return (
    <Screen footer={<BottomTabBar role="parent" accentColor={COLORS.pink} />}>
      <ModeBar label="부모님 모드 — 윤정아님" color={COLORS.pink} />

      <div className="flex items-center justify-between px-5 py-3.5">
        <h1 className="text-[22px] font-semibold text-remine-dark">추억 갤러리</h1>
        <button type="button" className="h-[38px] rounded-full bg-remine-pink px-4 text-[15px] font-semibold text-white">
          ＋ 추억 추가
        </button>
      </div>

      <div className="flex flex-col gap-4 px-5 pb-10">
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
            <p className="pt-0.5 text-[14px] text-white/50">2022년 봄 가족 여행 사진으로</p>
          </div>
          <span className="shrink-0 rounded-xl bg-remine-pink px-4 py-2.5 text-[15px] font-semibold text-white">풀기</span>
        </Link>

        <div className="grid grid-cols-2 gap-3">
          {MEMORIES.map((m) => (
            <div key={m.title} className="relative overflow-hidden rounded-[20px] bg-remine-surface">
              <div className="h-[130px] w-full overflow-hidden">
                <img src={m.photo} alt={m.title} className="size-full object-cover" />
              </div>
              <div className="flex flex-col gap-1 px-3 pb-3.5 pt-2.5">
                <p className="text-[14px] text-remine-dark">{m.title}</p>
                <p className="text-[13px] text-remine-muted">{m.date}</p>
              </div>
              {m.quizzed && (
                <span className="absolute right-2 top-2 flex size-[26px] items-center justify-center rounded-full bg-remine-pink text-white">
                  ✓
                </span>
              )}
            </div>
          ))}
        </div>

        <button
          type="button"
          className="flex h-14 items-center justify-center gap-1.5 rounded-2xl border border-dashed border-remine-highlight bg-white text-[17px] font-semibold text-remine-dark"
        >
          <span className="text-remine-pink">＋</span> 새 추억 사진 추가하기
        </button>
      </div>
    </Screen>
  )
}
