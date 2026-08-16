import StepLayout from '@/components/StepLayout'
import PillButton from '@/components/PillButton'
import type { Role } from '@/pages/onboarding/types'
import { AGE_GROUPS, ROLE_COLOR } from '@/pages/onboarding/types'
import { COLORS } from '@/theme'

type ProfileStepProps = {
  role: Role
  name: string
  ageGroup: string | null
  onChangeName: (name: string) => void
  onSelectAgeGroup: (age: string) => void
  onBack: () => void
  onSkip: () => void
  onNext: () => void
}

export default function ProfileStep({
  role,
  name,
  ageGroup,
  onChangeName,
  onSelectAgeGroup,
  onBack,
  onSkip,
  onNext,
}: ProfileStepProps) {
  const accentColor = ROLE_COLOR[role]

  return (
    <StepLayout
      dotIndex={1}
      accentColor={accentColor}
      onBack={onBack}
      onSkip={onSkip}
      heading="어떻게 불러드릴까요?"
      subtitle={role === 'parent' ? '부모님의 이름과 연령대를 알려주세요.' : '이름과 연령대를 알려주세요.'}
      footer={
        <PillButton onClick={onNext} disabled={!name || !ageGroup} color={accentColor}>
          다음
        </PillButton>
      }
    >
      <div className="w-full pb-2">
        <span className="text-[13px] leading-[1.5] text-remine-subtle">이름</span>
      </div>
      <input
        value={name}
        onChange={(e) => onChangeName(e.target.value)}
        placeholder="예) 홍길동"
        className="mb-6 h-[52px] w-full rounded-2xl border border-remine-border bg-remine-surface px-4 text-[16px] text-remine-dark placeholder:text-remine-borderSoft focus:outline-none"
      />
      <div className="w-full pb-3">
        <span className="text-[13px] leading-[1.5] text-remine-subtle">연령대</span>
      </div>
      <div className="grid w-full grid-cols-3 gap-2.5">
        {AGE_GROUPS.map((age) => {
          const selected = ageGroup === age
          return (
            <button
              key={age}
              type="button"
              onClick={() => onSelectAgeGroup(age)}
              className="flex h-11 items-center justify-center rounded-xl text-[14px]"
              style={{
                backgroundColor: selected ? accentColor : COLORS.surface,
                color: selected ? COLORS.white : COLORS.subtle,
                fontWeight: selected ? 600 : 400,
              }}
            >
              {age}
            </button>
          )
        })}
      </div>
    </StepLayout>
  )
}
