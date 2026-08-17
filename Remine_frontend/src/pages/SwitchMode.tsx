import { useLocation, useNavigate } from 'react-router-dom'
import type { Location } from 'react-router-dom'
import BottomSheet from '@/components/BottomSheet'
import { COLORS } from '@/theme'
import { useAuthStore } from '@/store/auth'
import type { Role } from '@/store/auth'

const PROFILE_META = {
  parent: { badge: '부모님 모드', color: COLORS.pink, emoji: '👩', desc: '오늘의 건강, 활동 분석, 추억 퀴즈', fallbackName: '부모님' },
  child: { badge: '자녀 모드', color: COLORS.blue, emoji: '👧', desc: '부모님 상태 모니터링, 사진 추가, 메시지', fallbackName: '자녀' },
} as const

export default function SwitchMode() {
  const navigate = useNavigate()
  const location = useLocation()
  const sessions = useAuthStore((s) => s.sessions)
  const setActiveRole = useAuthStore((s) => s.setActiveRole)
  const backgroundLocation = (location.state as { backgroundLocation?: Location } | null)?.backgroundLocation
  const originPathname = backgroundLocation?.pathname ?? window.location.pathname
  const current: string = originPathname.startsWith('/child') ? 'child' : 'parent'

  const profiles = (Object.keys(PROFILE_META) as Role[]).map((role) => ({
    role,
    name: sessions[role]?.name ?? PROFILE_META[role].fallbackName,
    ...PROFILE_META[role],
  }))

  const selectProfile = (role: Role) => {
    // This is the only place a user switches which paired demo account is
    // "active" — every API call afterwards authenticates as this role, so
    // this must run before navigating away, not just change the route.
    setActiveRole(role)
    navigate(`/${role}/home`)
  }

  return (
    <BottomSheet onDismiss={!backgroundLocation ? () => navigate(`/${current}/home`) : undefined}>
      <div className="flex flex-col gap-1">
        <h2 className="text-[21px] font-semibold text-remine-dark">누가 사용하고 있나요?</h2>
        <p className="text-[14px] text-remine-muted">모드에 따라 화면 구성이 달라져요.</p>
      </div>
      <div className="flex flex-col gap-3">
        {profiles.map((p) => {
          const selected = p.role === current
          return (
            <button
              key={p.role}
              type="button"
              onClick={() => selectProfile(p.role)}
              className="flex items-center gap-3.5 rounded-2xl border-2 p-4 text-left"
              style={{ borderColor: selected ? p.color : COLORS.border, backgroundColor: selected ? `${p.color}08` : COLORS.white }}
            >
              <div className="flex size-12 items-center justify-center rounded-full bg-remine-highlight text-xl">{p.emoji}</div>
              <div className="flex-1">
                <div className="flex items-center gap-2">
                  <span className="text-[16px] font-semibold text-remine-dark">{p.name}</span>
                  <span className="rounded-full px-2.5 py-0.5 text-[12px] font-semibold" style={{ backgroundColor: `${p.color}17`, color: p.color }}>
                    {p.badge}
                  </span>
                </div>
                <p className="pt-0.5 text-[13px] text-remine-muted">{p.desc}</p>
              </div>
              {selected && (
                <span className="flex size-6 shrink-0 items-center justify-center rounded-full text-white" style={{ backgroundColor: p.color }}>
                  ✓
                </span>
              )}
            </button>
          )
        })}
      </div>
    </BottomSheet>
  )
}
