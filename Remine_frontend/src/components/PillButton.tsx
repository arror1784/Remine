import type { ReactNode } from 'react'

type PillButtonProps = {
  children: ReactNode
  onClick?: () => void
  disabled?: boolean
  color?: string
}

export default function PillButton({ children, onClick, disabled, color = '#1a1a1a' }: PillButtonProps) {
  return (
    <div className="fixed inset-x-0 bottom-0 z-10 bg-gradient-to-t from-remine-bg from-60% to-transparent px-6 pb-[max(20px,env(safe-area-inset-bottom))] pt-6">
      <button
        type="button"
        onClick={onClick}
        disabled={disabled}
        className="flex h-[52px] w-full items-center justify-center rounded-full text-base font-semibold text-white disabled:opacity-40"
        style={{ backgroundColor: color }}
      >
        {children}
      </button>
    </div>
  )
}
