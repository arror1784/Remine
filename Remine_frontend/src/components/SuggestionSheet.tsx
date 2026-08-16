import { useNavigate } from 'react-router-dom'
import BottomSheet from '@/components/BottomSheet'
import { COLORS } from '@/theme'

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
  primaryTextColor = COLORS.white,
  onPrimary,
}: SuggestionSheetProps) {
  const navigate = useNavigate()

  return (
    <BottomSheet>
      <div className="flex size-12 items-center justify-center rounded-2xl bg-remine-surface text-xl">{emoji}</div>
      <div className="flex flex-col gap-2">
        <h2 className="text-[21px] font-semibold text-remine-dark">{title}</h2>
        <p className="text-[14px] leading-[1.5] text-remine-subtle">{description}</p>
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
        className="h-[52px] rounded-2xl bg-remine-surface text-[16px] font-semibold text-remine-dark"
      >
        다음에 할게요
      </button>
    </BottomSheet>
  )
}
