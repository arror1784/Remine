import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { COLORS } from '@/theme'

export interface NotificationItem {
  emoji: string
  bg: string
  title: string
  desc: string
  time: string
  unread: boolean
  to: string
}

export type Role = 'parent' | 'child'

const INITIAL_PARENT_NOTIFICATIONS: NotificationItem[] = [
  { emoji: '🌸', bg: COLORS.highlight, title: '딸 지영님이 사진을 추가했어요', desc: '2019년 봄 나들이 사진이 업로드됐어요.', time: '방금 전', unread: true, to: '/parent/memories' },
  { emoji: '🧩', bg: COLORS.highlight, title: '오늘의 추억 퀴즈가 준비됐어요', desc: '가족 나들이 사진으로 퀴즈를 풀어보세요.', time: '1시간 전', unread: true, to: '/parent/memories/quiz' },
  { emoji: '💬', bg: COLORS.highlight, title: '아들 민호님이 댓글을 남겼어요', desc: '이번 주말에 놀러갈게요!', time: '2시간 전', unread: true, to: '/parent/family' },
  { emoji: '🚶', bg: COLORS.surface, title: '오늘 걸음 수가 목표의 65%예요', desc: '조금만 더 걸으면 목표 달성이에요!', time: '오전 11시', unread: false, to: '/parent/today' },
  { emoji: '🌙', bg: COLORS.surface, title: '어젯밤 수면이 안정적이었어요', desc: '7시간 12분 — 평소와 비슷한 수면이에요.', time: '오전 8시', unread: false, to: '/parent/today' },
  { emoji: '👧', bg: COLORS.surface, title: '딸 지영님이 댓글을 달았어요', desc: '엄마 어제 산책 다녀오셨어요?', time: '어제', unread: false, to: '/parent/family' },
]

const INITIAL_CHILD_NOTIFICATIONS: NotificationItem[] = [
  { emoji: '⚠️', bg: COLORS.highlightBlue, title: '어머니 외출이 평소보다 적어요', desc: '오늘 아직 외출 기록이 없어요. 확인해보세요.', time: '방금 전', unread: true, to: '/child/today' },
  { emoji: '💬', bg: COLORS.highlight, title: '어머니가 메시지를 남기셨어요', desc: '지영아, 오늘 날씨 좋더라!', time: '30분 전', unread: true, to: '/child/family/message' },
  { emoji: '🧩', bg: COLORS.highlight, title: '어머니가 퀴즈를 완료하셨어요', desc: '3문제 중 2개 맞추셨어요! 🎉', time: '1시간 전', unread: true, to: '/child/today' },
  { emoji: '🚶', bg: COLORS.surface, title: '어머니 걸음 수 목표 달성!', desc: '오늘 8,200보 — 목표를 넘겼어요.', time: '오후 3시', unread: false, to: '/child/today' },
  { emoji: '🌙', bg: COLORS.surface, title: '어머니 수면 패턴이 안정적이에요', desc: '이번 주 평균 7시간 수면을 유지하고 있어요.', time: '오전 8시', unread: false, to: '/child/today' },
  { emoji: '📷', bg: COLORS.surface, title: '추억 사진이 추가됐어요', desc: '지영님이 올린 봄 나들이 사진을 확인해보세요.', time: '어제', unread: false, to: '/child/memories' },
]

export interface NotificationStore {
  parentNotifications: NotificationItem[]
  childNotifications: NotificationItem[]
  markParentAsRead: (index: number) => void
  markChildAsRead: (index: number) => void
  markAsRead: (role: Role, index: number) => void
  getParentUnreadCount: () => number
  getChildUnreadCount: () => number
  getUnreadCount: (role: Role) => number
}

export const useNotificationStore = create<NotificationStore>()(
  persist(
    (set, get) => ({
      parentNotifications: INITIAL_PARENT_NOTIFICATIONS,
      childNotifications: INITIAL_CHILD_NOTIFICATIONS,
      markParentAsRead: (index: number) =>
        set((state) => ({
          parentNotifications: state.parentNotifications.map((n, i) =>
            i === index ? { ...n, unread: false } : n
          ),
        })),
      markChildAsRead: (index: number) =>
        set((state) => ({
          childNotifications: state.childNotifications.map((n, i) =>
            i === index ? { ...n, unread: false } : n
          ),
        })),
      markAsRead: (role: Role, index: number) =>
        set((state) => {
          const key = role === 'parent' ? 'parentNotifications' : 'childNotifications'
          return {
            [key]: state[key].map((n, i) => (i === index ? { ...n, unread: false } : n)),
          }
        }),
      getParentUnreadCount: () => get().parentNotifications.filter((n) => n.unread).length,
      getChildUnreadCount: () => get().childNotifications.filter((n) => n.unread).length,
      getUnreadCount: (role: Role) =>
        get()[role === 'parent' ? 'parentNotifications' : 'childNotifications'].filter((n) => n.unread).length,
    }),
    { name: 'remine-notifications' }
  )
)
