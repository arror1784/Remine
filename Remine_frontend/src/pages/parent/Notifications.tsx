import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import BottomSheet from '@/components/BottomSheet'
import { getNotifications, markNotificationAsRead, type NotificationItem } from '@/api/notification'

function relativeTime(iso: string) {
  const minutes = Math.floor((Date.now() - new Date(iso).getTime()) / 60000)
  if (minutes < 1) return '방금 전'
  if (minutes < 60) return `${minutes}분 전`
  if (minutes < 60 * 24) return `${Math.floor(minutes / 60)}시간 전`
  return `${Math.floor(minutes / (60 * 24))}일 전`
}

export default function ParentNotifications() {
  const navigate = useNavigate()
  const [notifications, setNotifications] = useState<NotificationItem[]>([])
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const unreadCount = notifications.filter((n) => !n.read).length

  useEffect(() => {
    let active = true
    getNotifications()
      .then((data) => {
        if (active) setNotifications(data)
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

  const openNotification = (n: NotificationItem) => {
    markNotificationAsRead(n.id).catch(() => {})
    setNotifications((prev) => prev.map((item) => (item.id === n.id ? { ...item, read: true } : item)))
    navigate(`/parent/${n.deepLink}`)
  }

  return (
    <BottomSheet>
      <div className="flex items-center justify-between">
        <h1 className="flex items-center gap-2 text-[20px] font-semibold text-remine-dark">
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
          className="flex size-9 items-center justify-center rounded-full bg-remine-border text-remine-subtle"
        >
          ✕
        </button>
      </div>

      <div className="no-scrollbar flex max-h-[60vh] flex-col overflow-y-auto pl-2">
        {loading && <p className="py-10 text-center text-[15px] text-remine-muted">불러오는 중...</p>}

        {!loading && failed && <p className="py-10 text-center text-[15px] text-remine-muted">불러오지 못했어요</p>}

        {!loading && !failed && notifications.length === 0 && (
          <p className="py-10 text-center text-[15px] text-remine-muted">아직 알림이 없어요</p>
        )}

        {notifications.map((n, i) => (
          <button
            key={n.id}
            type="button"
            onClick={() => openNotification(n)}
            className={`relative flex gap-3.5 py-4 text-left ${i < notifications.length - 1 ? 'border-b border-remine-surfaceAlt' : ''}`}
          >
            {!n.read && <span className="absolute -left-2 top-5 size-1.5 rounded-full bg-remine-pink" />}
            <div className="flex size-11 shrink-0 items-center justify-center rounded-2xl text-[16px]" style={{ backgroundColor: n.bgColor }}>
              {n.emoji}
            </div>
            <div className="flex-1">
              <p className="text-[15px] font-semibold text-remine-dark">{n.title}</p>
              <p className="pt-0.5 text-[13px] leading-[1.4] text-remine-subtle">{n.description}</p>
              <p className="pt-1 text-[12px] text-remine-muted">{relativeTime(n.createdAt)}</p>
            </div>
          </button>
        ))}
      </div>
    </BottomSheet>
  )
}
