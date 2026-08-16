import { NavLink } from 'react-router-dom'
import { HomeIcon, ClockIcon, MemoryIcon, FamilyIcon } from '@/components/icons/NavIcons'
import type { ComponentType, CSSProperties } from 'react'
import { COLORS } from '@/theme'

type TabItem = {
  to: string
  label: string
  Icon: ComponentType<{ className?: string; style?: CSSProperties }>
}

const PARENT_TABS: TabItem[] = [
  { to: '/parent/home', label: '홈', Icon: HomeIcon },
  { to: '/parent/today', label: '오늘', Icon: ClockIcon },
  { to: '/parent/memories', label: '추억', Icon: MemoryIcon },
  { to: '/parent/family', label: '가족', Icon: FamilyIcon },
]

const CHILD_TABS: TabItem[] = [
  { to: '/child/home', label: '홈', Icon: HomeIcon },
  { to: '/child/today', label: '오늘', Icon: ClockIcon },
  { to: '/child/memories', label: '추억', Icon: MemoryIcon },
  { to: '/child/family', label: '가족', Icon: FamilyIcon },
]

type BottomTabBarProps = {
  role: 'parent' | 'child'
  accentColor: string
}

export default function BottomTabBar({ role, accentColor }: BottomTabBarProps) {
  const tabs = role === 'parent' ? PARENT_TABS : CHILD_TABS

  return (
    <div className="flex items-end justify-center border-t border-remine-border bg-white pb-[max(12px,env(safe-area-inset-bottom))] pt-px">
      {tabs.map(({ to, label, Icon }) => (
        <NavLink key={to} to={to} className="flex flex-1 flex-col items-center justify-center gap-1 pt-2">
          {({ isActive }) => (
            <>
              <Icon className="size-[22px]" style={{ color: isActive ? accentColor : COLORS.muted }} />
              <span className="text-[11px] font-medium" style={{ color: isActive ? accentColor : COLORS.muted }}>
                {label}
              </span>
            </>
          )}
        </NavLink>
      ))}
    </div>
  )
}
