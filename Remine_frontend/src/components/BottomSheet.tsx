import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'

type BottomSheetProps = {
  children: ReactNode
}

export default function BottomSheet({ children }: BottomSheetProps) {
  const navigate = useNavigate()

  return (
    <div className="flex h-full min-h-[500px] w-full flex-col justify-end bg-black/40" onClick={() => navigate(-1)}>
      <div
        className="flex flex-col gap-5 rounded-t-[28px] bg-remine-bg px-6 pb-8 pt-3"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="mx-auto h-1 w-10 rounded-full bg-[#ddddd5]" />
        {children}
      </div>
    </div>
  )
}
