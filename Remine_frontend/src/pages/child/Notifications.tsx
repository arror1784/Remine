import { useNavigate } from 'react-router-dom'
import BottomSheet from '@/components/BottomSheet'
import { useNotificationStore } from '@/store/notifications'

export default function ChildNotifications() {
  const navigate = useNavigate()
  const notifications = useNotificationStore((state) => state.childNotifications)
  const markChildAsRead = useNotificationStore((state) => state.markChildAsRead)
  const unreadCount = notifications.filter((n) => n.unread).length

  const openNotification = (index: number, to: string) => {
    markChildAsRead(index)
    navigate(to)
  }

  return (
    <BottomSheet>
      <div className="flex items-center justify-between">
        <h1 className="flex items-center gap-2 text-[20px] font-semibold text-[#1a1a1a]">
          지영님 알림
          {unreadCount > 0 && (
            <span className="flex size-5 items-center justify-center rounded-full bg-remine-blue text-[11px] font-semibold text-white">
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

      <div className="no-scrollbar flex max-h-[60vh] flex-col overflow-y-auto pl-2">
        {notifications.map((n, i) => (
          <button
            key={i}
            type="button"
            onClick={() => openNotification(i, n.to)}
            className={`relative flex gap-3.5 py-4 text-left ${i < notifications.length - 1 ? 'border-b border-[#f0f0ea]' : ''}`}
          >
            {n.unread && <span className="absolute -left-2 top-5 size-1.5 rounded-full bg-remine-blue" />}
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
    </BottomSheet>
  )
}
