import { Link } from 'react-router-dom'

type ModeBarProps = {
  label: string
  color: string
  dark?: boolean
}

export default function ModeBar({ label, color, dark }: ModeBarProps) {
  return (
    <div className="flex items-center justify-between px-5 pb-2 pt-[62px]">
      <div
        className="flex h-9 flex-1 items-center gap-2 rounded-xl px-3.5"
        style={{ backgroundColor: dark ? '#1c1c1c' : '#f2f2ee' }}
      >
        <span className="size-2 shrink-0 rounded-sm" style={{ backgroundColor: color }} />
        <span className="text-[13px]" style={{ color: dark ? '#ffffff' : '#1a1a1a' }}>
          {label}
        </span>
        <Link to="/switch-mode" className="ml-auto text-[12px]" style={{ color: dark ? 'rgba(255,255,255,0.4)' : '#9a9c91' }}>
          전환 ›
        </Link>
      </div>
    </div>
  )
}
