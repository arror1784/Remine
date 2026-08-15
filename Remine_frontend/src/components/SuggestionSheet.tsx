import { useNavigate } from 'react-router-dom'
import BottomSheet from '@/components/BottomSheet'

type SuggestionSheetProps = {
  emoji: string
  title: string
  description: string
  primaryLabel: string
  primaryColor: string
  primaryTextColor?: string
  onPrimary: () => void
}

export default function SuggestionSheet({
  emoji,
  title,
  description,
  primaryLabel,
  primaryColor,
  primaryTextColor = '#ffffff',
  onPrimary,
}: SuggestionSheetProps) {
  const navigate = useNavigate()

  return (
    <BottomSheet>
      <div className="flex size-12 items-center justify-center rounded-2xl bg-[#f2f2ee] text-xl">{emoji}</div>
      <div className="flex flex-col gap-2">
        <h2 className="text-[21px] font-semibold text-[#1a1a1a]">{title}</h2>
        <p className="text-[14px] leading-[1.5] text-[#66695d]">{description}</p>
      </div>
      <button
        type="button"
        onClick={onPrimary}
        className="h-[52px] rounded-2xl text-[16px] font-semibold"
        style={{ backgroundColor: primaryColor, color: primaryTextColor }}
      >
        {primaryLabel}
      </button>
      <button
        type="button"
        onClick={() => navigate(-1)}
        className="h-[52px] rounded-2xl bg-[#f2f2ee] text-[16px] font-semibold text-[#1a1a1a]"
      >
        다음에 할게요
      </button>
    </BottomSheet>
  )
}
