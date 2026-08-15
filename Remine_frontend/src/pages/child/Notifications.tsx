import { useNavigate } from 'react-router-dom'
import Screen from '@/components/Screen'

const NOTIFICATIONS = [
  { emoji: '⚠️', bg: '#e8f8ff', title: '어머니 외출이 평소보다 적어요', desc: '오늘 아직 외출 기록이 없어요. 확인해보세요.', time: '방금 전', unread: true },
  { emoji: '💬', bg: '#fff7cc', title: '어머니가 메시지를 남기셨어요', desc: '지영아, 오늘 날씨 좋더라!', time: '30분 전', unread: true },
  { emoji: '🧩', bg: '#fff7cc', title: '어머니가 퀴즈를 완료하셨어요', desc: '3문제 중 2개 맞추셨어요! 🎉', time: '1시간 전', unread: true },
  { emoji: '🚶', bg: '#f2f2ee', title: '어머니 걸음 수 목표 달성!', desc: '오늘 8,200보 — 목표를 넘겼어요.', time: '오후 3시', unread: false },
  { emoji: '🌙', bg: '#f2f2ee', title: '어머니 수면 패턴이 안정적이에요', desc: '이번 주 평균 7시간 수면을 유지하고 있어요.', time: '오전 8시', unread: false },
  { emoji: '📷', bg: '#f2f2ee', title: '추억 사진이 추가됐어요', desc: '지영님이 올린 봄 나들이 사진을 확인해보세요.', time: '어제', unread: false },
]

export default function ChildNotifications() {
  const navigate = useNavigate()
  const unreadCount = NOTIFICATIONS.filter((n) => n.unread).length

  return (
    <Screen>
      <div className="flex items-center justify-between px-5 pt-5">
        <h1 className="flex items-center gap-2 text-[20px] font-semibold text-[#1a1a1a]">
          지영님 알림
          <span className="flex size-5 items-center justify-center rounded-full bg-remine-blue text-[11px] font-semibold text-white">
            {unreadCount}
          </span>
        </h1>
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="flex size-9 items-center justify-center rounded-full bg-[#ebebeb] text-[#66695d]"
        >
          ✕
        </button>
      </div>

      <div className="flex flex-col px-5 pb-10 pt-4">
        {NOTIFICATIONS.map((n, i) => (
          <div key={i} className={`relative flex gap-3.5 py-4 ${i < NOTIFICATIONS.length - 1 ? 'border-b border-[#f0f0ea]' : ''}`}>
            {n.unread && <span className="absolute -left-2 top-5 size-1.5 rounded-full bg-remine-blue" />}
            <div className="flex size-11 shrink-0 items-center justify-center rounded-2xl text-[16px]" style={{ backgroundColor: n.bg }}>
              {n.emoji}
            </div>
            <div className="flex-1">
              <p className="text-[15px] font-semibold text-[#1a1a1a]">{n.title}</p>
              <p className="pt-0.5 text-[13px] leading-[1.4] text-[#66695d]">{n.desc}</p>
              <p className="pt-1 text-[12px] text-[#9a9c91]">{n.time}</p>
            </div>
          </div>
        ))}
      </div>
    </Screen>
  )
}
