type ModeBarProps = {
  label: string
  color: string
}

export default function ModeBar({ label, color }: ModeBarProps) {
  return (
    <div className="flex items-center justify-between px-5 pb-2 pt-[62px]">
      <div className="flex h-9 flex-1 items-center gap-2 rounded-xl bg-[#f2f2ee] px-3.5">
        <span className="size-2 shrink-0 rounded-sm" style={{ backgroundColor: color }} />
        <span className="text-[13px] text-[#1a1a1a]">{label}</span>
        <button type="button" className="ml-auto text-[12px] text-[#9a9c91]">
          전환 ›
        </button>
      </div>
    </div>
  )
}
