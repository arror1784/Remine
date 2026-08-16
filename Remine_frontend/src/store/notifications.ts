import { create } from 'zustand'
import { getUnreadCount } from '@/api/notification'

export interface NotificationStore {
  unreadCount: number
  refreshUnreadCount: () => Promise<void>
  decrementUnreadCount: (by: number) => void
}

// Home stays mounted underneath the notifications bottom sheet (it's rendered
// as a background route), so the bell badge can't rely on remounting to pick
// up reads that happen in the sheet — it needs this shared store instead.
export const useNotificationStore = create<NotificationStore>((set) => ({
  unreadCount: 0,
  refreshUnreadCount: async () => {
    try {
      const count = await getUnreadCount()
      set({ unreadCount: count })
    } catch {
      // keep the last known count on failure
    }
  },
  decrementUnreadCount: (by) => set((state) => ({ unreadCount: Math.max(0, state.unreadCount - by) })),
}))
