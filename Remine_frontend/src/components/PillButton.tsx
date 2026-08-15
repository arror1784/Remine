import type { ReactNode } from 'react'

type PillButtonProps = {
  children: ReactNode
  onClick?: () => void
  disabled?: boolean
  color?: string
}

export default function PillButton({ children, onClick, disabled, color = '#1a1a1a' }: PillButtonProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className="absolute bottom-[52px] left-6 right-6 flex h-[52px] items-center justify-center rounded-full text-base font-semibold text-white disabled:opacity-40"
      style={{ backgroundColor: color }}
    >
      {children}
    </button>
  )
}
