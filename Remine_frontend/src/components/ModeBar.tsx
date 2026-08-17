import { Link, useLocation } from 'react-router-dom'

type ModeBarProps = {
  label: string
  color: string
  dark?: boolean
}

export default function ModeBar({ label, color, dark = false }: ModeBarProps) {
  const location = useLocation()

  return (
    // pt-[62px] clears a real device's notch below `sm` (StatusBar is hidden
    // there, so this is the only top clearance). At `sm` and up StatusBar
    // already reserves its own 44px row above this, so a big top padding
    // here on top of that reads as a huge empty gap — just a small one.
    <div className="flex items-center justify-between px-5 pb-2 pt-[62px] sm:pt-3">
      <div
        className={`flex h-9 flex-1 items-center gap-2 rounded-xl px-3.5 ${
          dark ? 'bg-remine-surfaceDark text-white' : 'bg-remine-surface text-remine-dark'
        }`}
      >
        <span className="size-2 shrink-0 rounded-sm" style={{ backgroundColor: color }} />
        <span className="text-[13px]">
          {label}
        </span>
        <Link
          to="/switch-mode"
          state={{ backgroundLocation: location }}
          className={`ml-auto text-[12px] ${dark ? 'text-white/40' : 'text-remine-muted'}`}
        >
          전환 ›
        </Link>
      </div>
    </div>
  )
}
