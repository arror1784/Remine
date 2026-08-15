import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Screen from '@/components/Screen'

const INITIAL_NOTIFICATIONS = [
  { emoji: '🌸', bg: '#fff7cc', title: '딸 지영님이 사진을 추가했어요', desc: '2019년 봄 나들이 사진이 업로드됐어요.', time: '방금 전', unread: true, to: '/parent/memories' },
  { emoji: '🧩', bg: '#fff7cc', title: '오늘의 추억 퀴즈가 준비됐어요', desc: '가족 나들이 사진으로 퀴즈를 풀어보세요.', time: '1시간 전', unread: true, to: '/parent/memories/quiz' },
  { emoji: '💬', bg: '#fff7cc', title: '아들 민호님이 댓글을 남겼어요', desc: '이번 주말에 놀러갈게요!', time: '2시간 전', unread: true, to: '/parent/family' },
  { emoji: '🚶', bg: '#f2f2ee', title: '오늘 걸음 수가 목표의 65%예요', desc: '조금만 더 걸으면 목표 달성이에요!', time: '오전 11시', unread: false, to: '/parent/today' },
  { emoji: '🌙', bg: '#f2f2ee', title: '어젯밤 수면이 안정적이었어요', desc: '7시간 12분 — 평소와 비슷한 수면이에요.', time: '오전 8시', unread: false, to: '/parent/today' },
  { emoji: '👧', bg: '#f2f2ee', title: '딸 지영님이 댓글을 달았어요', desc: '엄마 어제 산책 다녀오셨어요?', time: '어제', unread: false, to: '/parent/family' },
]

export default function ParentNotifications() {
  const navigate = useNavigate()
  const [notifications, setNotifications] = useState(INITIAL_NOTIFICATIONS)
  const unreadCount = notifications.filter((n) => n.unread).length

  const openNotification = (index: number, to: string) => {
    setNotifications((prev) => prev.map((n, i) => (i === index ? { ...n, unread: false } : n)))
    navigate(to)
  }

  return (
    <Screen>
      <div className="flex items-center justify-between px-5 pt-5">
        <h1 className="flex items-center gap-2 text-[20px] font-semibold text-[#1a1a1a]">
          알림
          {unreadCount > 0 && (
            <span className="flex size-5 items-center justify-center rounded-full bg-remine-pink text-[11px] font-semibold text-white">
              {unreadCount}
            </span>
          )}
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
        {notifications.map((n, i) => (
          <button
            key={i}
            type="button"
            onClick={() => openNotification(i, n.to)}
            className={`relative flex gap-3.5 py-4 text-left ${i < notifications.length - 1 ? 'border-b border-[#f0f0ea]' : ''}`}
          >
            {n.unread && <span className="absolute -left-2 top-5 size-1.5 rounded-full bg-remine-pink" />}
            <div className="flex size-11 shrink-0 items-center justify-center rounded-2xl text-[16px]" style={{ backgroundColor: n.bg }}>
              {n.emoji}
            </div>
            <div className="flex-1">
              <p className="text-[15px] font-semibold text-[#1a1a1a]">{n.title}</p>
              <p className="pt-0.5 text-[13px] leading-[1.4] text-[#66695d]">{n.desc}</p>
              <p className="pt-1 text-[12px] text-[#9a9c91]">{n.time}</p>
            </div>
          </button>
        ))}
      </div>
    </Screen>
  )
}
