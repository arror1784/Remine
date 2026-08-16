import { useState, useEffect, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'

type BottomSheetProps = {
  children: ReactNode
}

export default function BottomSheet({ children }: BottomSheetProps) {
  const navigate = useNavigate()
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    const rafId = requestAnimationFrame(() => {
      setMounted(true)
    })
    return () => cancelAnimationFrame(rafId)
  }, [])

  return (
    <div
      className={`flex h-full min-h-[500px] w-full flex-col justify-end transition-colors duration-300 ${
        mounted ? 'bg-black/40' : 'bg-black/0'
      }`}
      onClick={() => navigate(-1)}
    >
      <div
        className={`flex flex-col gap-5 rounded-t-[28px] bg-remine-bg px-6 pb-8 pt-3 transition-transform duration-300 ease-out transform ${
          mounted ? 'translate-y-0' : 'translate-y-full'
        }`}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="mx-auto h-1 w-10 rounded-full bg-remine-borderMuted" />
        {children}
      </div>
    </div>
  )
}

