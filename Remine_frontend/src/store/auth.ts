import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type Role = 'parent' | 'child'

export interface DemoSession {
  userId: string
  name: string
  accessToken: string
  pairedUserId: string | null
}

export interface AuthStore {
  sessions: { parent?: DemoSession; child?: DemoSession }
  activeRole: Role
  setSession: (role: Role, session: DemoSession) => void
  setActiveRole: (role: Role) => void
  getActiveToken: () => string | undefined
  clearSessions: () => void
}

export const useAuthStore = create<AuthStore>()(
  persist(
    (set, get) => ({
      sessions: {},
      activeRole: 'parent',
      setSession: (role: Role, session: DemoSession) =>
        set((state) => ({ sessions: { ...state.sessions, [role]: session } })),
      setActiveRole: (activeRole: Role) => set({ activeRole }),
      getActiveToken: () => get().sessions[get().activeRole]?.accessToken,
      clearSessions: () => set({ sessions: {} }),
    }),
    { name: 'remine-auth' }
  )
)
