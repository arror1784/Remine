import { Link } from 'react-router-dom'
import Screen from '@/components/Screen'
import ModeBar from '@/components/ModeBar'
import BottomTabBar from '@/components/BottomTabBar'
import { BellIcon } from '@/components/icons/NavIcons'
import springOuting from '@/assets/memories/family-trip.png'
import birthdayCake from '@/assets/memories/birthday-cake.png'
import grandchildWalk from '@/assets/memories/grandchild-walk.png'

const ACTIVITIES = [
  { emoji: '🌙', label: '수면', value: '7시간 12분', percent: 90, barColor: '#37ceff', ok: true },
  { emoji: '👟', label: '걸음', value: '4,280보', percent: 55, barColor: '#ffb84d', ok: false },
  { emoji: '🌿', label: '외출', value: '1회', percent: 40, barColor: '#ffb84d', ok: false },
  { emoji: '💬', label: '사회', value: '0회', percent: 10, barColor: '#ddddd5', ok: false },
]

const WEEK_PATTERN = [
  { day: '수', height: 34 },
  { day: '목', height: 37 },
  { day: '금', height: 28 },
  { day: '토', height: 40 },
  { day: '일', height: 21 },
  { day: '월', height: 44 },
  { day: '오늘', height: 37, today: true },
]

const MEMORY_SHORTCUTS = [
  { photo: springOuting, label: '봄 나들이' },
  { photo: birthdayCake, label: '어머니 생신' },
  { photo: grandchildWalk, label: '손주와 산책' },
]

export default function ChildHome() {
  return (
    <Screen footer={<BottomTabBar role="child" accentColor="#37ceff" />}>
      <ModeBar label="자녀 모드 — 지영님" color="#37ceff" dark />

      <div className="flex items-center justify-between px-5 py-3.5">
        <span className="text-[19px] font-semibold text-[#1a1a1a]">
          Rem<span className="text-remine-pink">e</span>ine
        </span>
        <div className="flex items-center gap-2">
          <Link to="/child/notifications" className="relative flex items-start p-1">
            <BellIcon className="size-6 text-[#1a1a1a]" />
            <span className="absolute right-[6.75px] top-[4.7px] size-2 rounded-full border-2 border-remine-bg bg-remine-pink" />
          </Link>
          <Link to="/child/mypage" className="flex size-9 items-center justify-center rounded-full bg-[#fff7cc] text-[14px]">
            👧
          </Link>
        </div>
      </div>

      <div className="flex flex-col gap-6 px-5 pb-8">
        <div className="flex flex-col gap-1">
          <p className="text-[16px] text-[#9a9c91]">2026년 8월 11일 화요일</p>
          <h1 className="text-[25px] font-semibold text-[#1a1a1a]">안녕하세요, 지영님 👋</h1>
        </div>

        <div className="relative overflow-hidden rounded-3xl bg-[#1c1c1c] p-6">
          <div aria-hidden className="absolute -right-5 -top-7 size-[120px] rounded-full bg-remine-blue opacity-10 blur-[18px]" />
          <div aria-hidden className="absolute -bottom-5 left-5 size-20 rounded-full bg-remine-pink opacity-10 blur-[14px]" />
          <div className="flex items-center gap-3.5">
            <div className="flex size-[52px] items-center justify-center rounded-full bg-[#fff7cc] text-xl">👩</div>
            <div className="flex-1">
              <p className="text-[13px] text-white/40">오늘의 어머니 상태</p>
              <p className="text-[18px] font-semibold text-white">윤정아님</p>
            </div>
            <span className="flex items-center gap-1.5 rounded-full bg-remine-blue/15 px-3 py-1.5 text-[12px] font-semibold text-remine-blue">
              <span className="size-1.5 rounded-full bg-remine-blue" />
              안정
            </span>
          </div>
          <p className="pt-4 text-[15px] leading-[1.5] text-white/45">오늘 수면이 안정적이에요. 걸음 수가 조금 적으니 산책을 권해드려 보세요.</p>
          <div className="flex gap-2.5 pt-4">
            <Link
              to="/child/family/message"
              className="flex h-11 flex-1 items-center justify-center rounded-xl bg-remine-blue text-[13.5px] font-semibold text-white"
            >
              💬 메시지 보내기
            </Link>
            <Link
              to="/child/family/call"
              className="flex h-11 flex-1 items-center justify-center rounded-xl border border-white/15 bg-white/10 text-[13.5px] font-semibold text-white"
            >
              📞 전화하기
            </Link>
          </div>
        </div>

        <div className="flex gap-3 rounded-[20px] bg-[#fff7cc] px-5 py-4">
          <span className="mt-0.5 size-2 shrink-0 rounded-sm bg-remine-pink" />
          <div>
            <p className="text-[15px] font-semibold text-[#1a1a1a]">오늘 외출이 평소보다 적어요</p>
            <p className="pt-1 text-[14px] leading-[1.5] text-[#66695d]">어머니가 오늘 아직 외출을 못 하셨어요. 가벼운 산책을 권해드려 보세요.</p>
          </div>
        </div>

        <div className="flex flex-col gap-1">
          <h2 className="pb-2 text-[20px] font-semibold text-[#1a1a1a]">어머니 오늘의 활동</h2>
          <div className="flex flex-col rounded-[20px] border border-[#ebebeb] bg-white">
            {ACTIVITIES.map((a, i) => (
              <div key={a.label} className={`flex items-center gap-3.5 px-5 py-4 ${i < ACTIVITIES.length - 1 ? 'border-b border-[#f4f4f0]' : ''}`}>
                <span className="text-[15px]">{a.emoji}</span>
                <div className="flex flex-1 flex-col gap-1.5">
                  <div className="flex items-center justify-between text-[15px] text-[#1a1a1a]">
                    <span>{a.label}</span>
                    <span>{a.value}</span>
                  </div>
                  <div className="h-1.5 overflow-hidden rounded-full bg-[#f0f0ea]">
                    <div className="h-full rounded-full" style={{ width: `${a.percent}%`, backgroundColor: a.barColor }} />
                  </div>
                </div>
                <span
                  className="flex size-5 items-center justify-center rounded-[10px] text-[11px]"
                  style={{ backgroundColor: a.ok ? '#e8f8ff' : '#fff4e0' }}
                >
                  {a.ok ? '✓' : '!'}
                </span>
              </div>
            ))}
          </div>
        </div>

        <div className="flex flex-col gap-4">
          <h2 className="text-[20px] font-semibold text-[#1a1a1a]">이번 주 패턴</h2>
          <div className="flex flex-col gap-4 rounded-[20px] border border-[#ebebeb] bg-white p-5">
            <div className="flex items-end justify-center gap-1.5">
              {WEEK_PATTERN.map(({ day, height, today }) => (
                <div key={day} className="flex w-10 flex-col items-center gap-1.5">
                  <div
                    className="w-full rounded"
                    style={{ height: `${height}px`, backgroundColor: today ? '#37ceff' : '#f0f0ea' }}
                  />
                  <span className="text-[10px]" style={{ color: today ? '#37ceff' : '#9a9c91' }}>
                    {day}
                  </span>
                </div>
              ))}
            </div>
            <div className="flex gap-2">
              <span className="mt-1.5 size-2.5 shrink-0 rounded-sm bg-remine-blue" />
              <p className="text-[13px] leading-[1.4] text-[#66695d]">이번 주 전반적으로 안정적이에요. 일요일 활동이 낮았으니 참고해주세요.</p>
            </div>
          </div>
        </div>

        <div className="flex flex-col gap-3">
          <div className="flex items-baseline gap-2.5">
            <h2 className="text-[20px] font-semibold text-[#1a1a1a]">추억 사진 추가</h2>
            <span className="text-[13px] text-[#9a9c91]">어머니 퀴즈에 활용돼요</span>
          </div>
          <div className="flex gap-2.5 overflow-x-auto pb-1">
            {MEMORY_SHORTCUTS.map((m) => (
              <div key={m.label} className="w-[110px] shrink-0 overflow-hidden rounded-2xl bg-[#f2f2ee]">
                <img src={m.photo} alt={m.label} className="h-[90px] w-full object-cover" />
                <p className="px-2.5 py-2 text-[12px] text-[#1a1a1a]">{m.label}</p>
              </div>
            ))}
          </div>
        </div>

        <Link
          to="/child/family/message"
          className="flex h-[52px] items-center justify-center gap-2 rounded-2xl bg-[#1a1a1a] text-[16px] font-semibold text-white"
        >
          💬 어머니께 메시지 보내기
        </Link>
      </div>
    </Screen>
  )
}
