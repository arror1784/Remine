import Screen from '@/components/Screen'
import ModeBar from '@/components/ModeBar'
import BottomTabBar from '@/components/BottomTabBar'

const CHECKLIST = [
  { emoji: '🌙', label: '수면', status: '완료', done: true, desc: '오전 7:14 기상', bg: '#e8f8ff' },
  { emoji: '🍚', label: '아침식사', status: '미완료', done: false, desc: '아직 기록 없어요', bg: '#f7f7f3' },
  { emoji: '🚶', label: '산책', status: '미완료', done: false, desc: '목표의 55%', bg: '#f7f7f3' },
  { emoji: '🧩', label: '퀴즈', status: '미완료', done: false, desc: '오늘 퀴즈 대기 중', bg: '#f7f7f3' },
]

const TIMELINE = [
  { time: '오전 7:14', label: '기상 확인됨', color: '#37ceff' },
  { time: '오전 9:02', label: '앱 열어보심', color: '#37ceff' },
  { time: '오전 10:30', label: '산책 시작 (4,280보)', color: '#ffb84d' },
  { time: '오후 12:00', label: '이후 활동 없음', color: '#ddddd5', faint: true },
]

export default function ChildToday() {
  return (
    <Screen footer={<BottomTabBar role="child" accentColor="#37ceff" />}>
      <ModeBar label="자녀 모드 — 지영님" color="#37ceff" dark />

      <div className="px-5 py-3.5">
        <h1 className="text-[22px] font-semibold text-[#1a1a1a]">어머니 오늘</h1>
      </div>

      <div className="flex flex-col gap-6 px-5 pb-8">
        <div className="flex flex-col gap-2 rounded-3xl bg-[#1c1c1c] px-6 py-[22px]">
          <p className="text-[13px] text-white/40">2026년 8월 11일 화요일</p>
          <p className="text-[22px] font-semibold text-white">오늘 1/4 완료됐어요</p>
          <div className="h-2 overflow-hidden rounded-full bg-white/10">
            <div className="h-full w-1/4 rounded-full bg-remine-blue" />
          </div>
          <p className="text-[13px] text-white/40">3가지 활동이 아직 남았어요</p>
        </div>

        <div className="flex flex-col gap-2.5">
          <h2 className="pb-1 text-[18px] font-semibold text-[#1a1a1a]">활동 체크리스트</h2>
          {CHECKLIST.map((item) => (
            <div key={item.label} className="flex items-center gap-3.5 rounded-2xl border border-[#ebebeb] bg-white px-[18px] py-4">
              <div className="flex size-11 items-center justify-center rounded-2xl text-[17px]" style={{ backgroundColor: item.bg }}>
                {item.emoji}
              </div>
              <div className="flex flex-1 flex-col gap-0.5">
                <div className="flex items-center gap-2">
                  <span className="text-[16px] font-medium text-[#1a1a1a]">{item.label}</span>
                  <span
                    className="rounded-full px-2 py-0.5 text-[12px] font-semibold"
                    style={{ backgroundColor: item.done ? '#e8f8ff' : '#f2f2ee', color: item.done ? '#37ceff' : '#9a9c91' }}
                  >
                    {item.status}
                  </span>
                </div>
                <span className="text-[13px] text-[#9a9c91]">{item.desc}</span>
              </div>
              {item.done ? (
                <span className="flex size-7 items-center justify-center rounded-full bg-remine-blue text-white">✓</span>
              ) : (
                <button type="button" className="h-[34px] shrink-0 rounded-[10px] bg-[#fff7cc] px-3 text-[13px] font-semibold text-[#1a1a1a]">
                  응원 보내기
                </button>
              )}
            </div>
          ))}
        </div>

        <div className="flex flex-col gap-3">
          <h2 className="text-[18px] font-semibold text-[#1a1a1a]">오늘의 타임라인</h2>
          <div className="flex flex-col gap-[18px] rounded-[20px] border border-[#ebebeb] bg-white px-5 py-[18px]">
            {TIMELINE.map((t, i) => (
              <div key={t.time} className="relative flex gap-4">
                <div className="flex w-3 flex-col items-center pt-1">
                  <span className="size-3 shrink-0 rounded-full" style={{ backgroundColor: t.color }} />
                  {i < TIMELINE.length - 1 && <span className="mt-1 h-8 w-0.5 bg-[#f0f0ea]" />}
                </div>
                <div className="flex flex-col gap-0.5">
                  <span className="text-[12px] text-[#9a9c91]">{t.time}</span>
                  <span className="text-[14px] font-medium" style={{ color: t.faint ? '#bbbbb3' : '#1a1a1a' }}>
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
