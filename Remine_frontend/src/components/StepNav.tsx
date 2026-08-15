import chevron from '@/assets/chevron.svg'

type StepNavProps = {
  onBack?: () => void
  onSkip?: () => void
}

export default function StepNav({ onBack, onSkip }: StepNavProps) {
  return (
    <div className="flex w-full items-center justify-between px-[18px] pt-[28px]">
      <button
        type="button"
        aria-label="이전"
        onClick={onBack}
        disabled={!onBack}
        className="flex size-[18px] items-center justify-center disabled:opacity-0"
      >
        <img src={chevron} alt="" className="size-[6px] rotate-180" />
      </button>
      <button
        type="button"
        aria-label="다음으로 건너뛰기"
        onClick={onSkip}
        disabled={!onSkip}
        className="flex size-[18px] items-center justify-center disabled:opacity-0"
      >
        <img src={chevron} alt="" className="size-[6px]" />
      </button>
    </div>
  )
}
