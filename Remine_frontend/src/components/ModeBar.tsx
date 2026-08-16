import { Link, useLocation } from 'react-router-dom'
import { COLORS } from '@/theme'

type ModeBarProps = {
  label: string
  color: string
  dark?: boolean
}

export default function ModeBar({ label, color, dark }: ModeBarProps) {
  const location = useLocation()

  return (
    <div className="flex items-center justify-between px-5 pb-2 pt-[62px]">
      <div
        className="flex h-9 flex-1 items-center gap-2 rounded-xl px-3.5"
        style={{ backgroundColor: dark ? COLORS.surfaceDark : COLORS.surface }}
      >
        <span className="size-2 shrink-0 rounded-sm" style={{ backgroundColor: color }} />
        <span className="text-[13px]" style={{ color: dark ? COLORS.white : COLORS.dark }}>
          {label}
        </span>
        <Link
          to="/switch-mode"
          state={{ backgroundLocation: location }}
          className="ml-auto text-[12px]"
          style={{ color: dark ? 'rgba(255,255,255,0.4)' : COLORS.muted }}
        >
          전환 ›
        </Link>
      </div>
    </div>
  )
}
