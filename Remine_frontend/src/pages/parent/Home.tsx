import { Link, useLocation } from 'react-router-dom'
import Screen from '@/components/Screen'
import BottomTabBar from '@/components/BottomTabBar'
import ModeBar from '@/components/ModeBar'
import { BellIcon } from '@/components/icons/NavIcons'
import { useNotificationStore } from '@/store/notifications'
import familyPhoto from '@/assets/memories/family-trip.png'

const WEEK_PATTERN = [
  { day: '수', height: 24 },
  { day: '목', height: 27 },
  { day: '금', height: 20 },
  { day: '토', height: 29 },
  { day: '일', height: 15 },
  { day: '월', height: 32 },
  { day: '오늘', height: 27, today: true },
]

const ACTIVITIES = [
  { emoji: '🌙', label: '수면', value: '7시간 12분', percent: 90, barColor: '#37ceff', note: '평소와 비슷', noteColor: '#2ba8d4' },
  { emoji: '👟', label: '걸음', value: '4,280보', percent: 55, barColor: '#ffb84d', note: '조금 적어요', noteColor: '#c4901f' },
  { emoji: '🌿', label: '외출', value: '1회', percent: 40, barColor: '#ffb84d', note: '평소보다 적음', noteColor: '#c4901f' },
  { emoji: '💬', label: '대화', value: '0회', percent: 10, barColor: '#ddddd5', note: '오늘은 휴식', noteColor: '#9a9c91' },
]

export default function ParentHome() {
  const location = useLocation()
  const unreadCount = useNotificationStore((state) => state.parentNotifications.filter((n) => n.unread).length)

  return (
    <Screen footer={<BottomTabBar role="parent" accentColor="#ff42ad" />}>
      <ModeBar label="부모님 모드 — 윤정아님" color="#ff42ad" />

      <div className="flex items-center justify-between px-5 py-3.5">
        <span className="text-[19px] font-semibold text-[#1a1a1a]">
          Rem<span className="text-remine-pink">e</span>ine
        </span>
        <div className="flex items-center gap-2">
          <Link to="/parent/notifications" state={{ backgroundLocation: location }} className="relative flex items-start p-1">
            <BellIcon className="size-6 text-[#1a1a1a]" />
            {unreadCount > 0 && (
              <span className="absolute right-[6.75px] top-[4.7px] size-2 rounded-full border-2 border-remine-bg bg-remine-pink" />
            )}
          </Link>
          <Link to="/parent/mypage" className="flex size-9 items-center justify-center rounded-full bg-[#fff7cc] text-[14px]">
            👩
          </Link>
        </div>
      </div>

      <div className="flex flex-col gap-7 px-5 pb-8">
        <div className="flex flex-col gap-1">
          <p className="text-[16px] text-[#9a9c91]">2026년 8월 11일 화요일</p>
          <h1 className="text-[26px] font-semibold leading-[1.3] text-[#1a1a1a]">
            좋은 아침이에요,
            <br />
            윤정아님 👋
          </h1>
        </div>

        <div className="relative overflow-hidden rounded-3xl bg-[#1c1c1c] px-6 py-7">
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
                    className="h-8 w-full rounded"
                    style={{
                      backgroundColor: today ? '#ff42ad' : 'rgba(55,206,255,0.18)',
                      height: `${height}px`,
                      alignSelf: 'flex-end',
                    }}
                  />
                  <span className="text-[10px]" style={{ color: today ? '#ff42ad' : 'rgba(255,255,255,0.28)' }}>
                    {day}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>

        <div className="flex flex-col">
          <h2 className="pb-1 text-[20px] font-semibold text-[#1a1a1a]">오늘의 활동</h2>
          {ACTIVITIES.map((a, i) => (
            <div
              key={a.label}
              className={`flex items-center gap-4 py-[18px] ${i < ACTIVITIES.length - 1 ? 'border-b border-[#ebebeb]' : ''}`}
            >
              <div className="flex size-12 shrink-0 items-center justify-center rounded-2xl bg-[#f2f2ee] text-[17px]">{a.emoji}</div>
              <div className="flex flex-1 flex-col gap-2">
                <div className="flex items-center justify-between">
                  <span className="text-[16px] text-[#1a1a1a]">{a.label}</span>
                  <span className="text-[18px] text-[#1a1a1a]">{a.value}</span>
                </div>
                <div className="flex items-center gap-2.5">
                  <div className="h-1.5 flex-1 overflow-hidden rounded-full bg-[#ebebeb]">
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

        <div className="flex flex-col gap-2 rounded-[20px] bg-[#fff7cc] px-5 pb-5 pt-6">
          <div className="flex items-center gap-2">
            <span className="size-2 rounded-sm bg-remine-pink" />
            <span className="text-[13px] font-semibold tracking-wide text-[#1a1a1a]">AI 추천</span>
          </div>
          <p className="pt-1 text-[20px] font-semibold text-[#1a1a1a]">오늘 오후 산책 어떠세요?</p>
          <p className="pb-3 text-[15px] leading-[1.5] text-[#66695d]">걸음 수가 평소보다 적어요. 20분 가벼운 산책이 기분 전환에 도움이 돼요.</p>
          <Link
            to="/parent/reminders/walk"
            state={{ backgroundLocation: location }}
            className="flex h-[52px] items-center justify-center rounded-2xl bg-[#1a1a1a] text-[17px] font-semibold text-white"
          >
            산책 시작하기
          </Link>
        </div>

        <div className="flex flex-col gap-4">
          <h2 className="text-[20px] font-semibold text-[#1a1a1a]">오늘의 추억</h2>
          <div className="overflow-hidden rounded-[20px] border border-[#ebebeb] bg-white">
            <div className="h-[164px] w-full overflow-hidden bg-[#fff7cc]">
              <img src={familyPhoto} alt="가족 나들이 사진" className="size-full object-cover" />
            </div>
            <div className="flex flex-col gap-1.5 px-5 pb-5 pt-[18px]">
              <span className="w-fit rounded-full bg-[#fff7cc] px-2.5 py-0.5 text-[12px] text-[#1a1a1a]">2022년 봄</span>
              <p className="pt-1 text-[17px] text-[#1a1a1a]">가족 여행 📸</p>
              <p className="pb-2 text-[15px] text-[#66695d]">이 사진과 관련된 퀴즈를 풀어볼까요?</p>
              <Link
                to="/parent/memories/quiz"
                className="flex h-12 items-center justify-center rounded-xl bg-[#fff7cc] text-[16px] font-semibold text-[#1a1a1a]"
              >
                추억 퀴즈 풀기
              </Link>
            </div>
          </div>
        </div>
      </div>
    </Screen>
  )
}
