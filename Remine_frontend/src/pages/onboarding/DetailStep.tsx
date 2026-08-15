import StepLayout from '@/components/StepLayout'
import PillButton from '@/components/PillButton'
import type { Role } from '@/pages/onboarding/types'
import { PARENT_INTERESTS, ROLE_COLOR } from '@/pages/onboarding/types'

type DetailStepProps = {
  role: Role
  interests: string[]
  onToggleInterest: (interest: string) => void
  inviteCode: string
  onChangeInviteCode: (code: string) => void
  onBack: () => void
  onSkip?: () => void
  onNext: () => void
}

export default function DetailStep({
  role,
  interests,
  onToggleInterest,
  inviteCode,
  onChangeInviteCode,
  onBack,
  onSkip,
  onNext,
}: DetailStepProps) {
  const accentColor = ROLE_COLOR[role]

  if (role === 'child') {
    const connected = inviteCode.trim().length > 0

    return (
      <StepLayout
        dotIndex={2}
        accentColor={accentColor}
        onBack={onBack}
        heading="부모님과 연결해요!"
        subtitle="부모님 앱에서 발급된 초대 코드를 입력해 주세요."
        footer={
          <PillButton onClick={onNext} disabled={!connected} color={accentColor}>
            연결하기
          </PillButton>
        }
      >
        <div className="w-full pb-2">
          <span className="text-[13px] leading-[1.5] text-[#66695d]">초대 코드</span>
        </div>
        <input
          value={inviteCode}
          onChange={(e) => onChangeInviteCode(e.target.value.toUpperCase())}
          placeholder="REMIND-XXXX"
          className="mb-3 h-14 w-full rounded-2xl border-2 bg-[#f2f2ee] px-[18px] text-[18px] tracking-[1.8px] text-[#1a1a1a] placeholder:text-[#bbbbb3] focus:outline-none"
          style={{ borderColor: accentColor }}
        />
        {connected && (
          <div
            className="mb-8 flex h-9 w-full items-center justify-center gap-1.5 rounded-xl text-[13px]"
            style={{ backgroundColor: `${accentColor}17`, color: accentColor }}
          >
            <span>✓</span>
            <span>부모님 계정과 연결됩니다</span>
          </div>
        )}
        <div className="w-full rounded-[20px] bg-[#f7f7f3] px-[18px] py-4">
          <p className="pb-1.5 text-[13px] leading-[1.5] text-[#66695d]">초대 코드는 어디서 받나요?</p>
          <p className="text-[13px] leading-[1.55] text-[#9a9c91]">부모님 앱 → 마이페이지 → 초대 코드에서 확인할 수 있어요.</p>
        </div>
      </StepLayout>
    )
  }

  return (
    <StepLayout
      dotIndex={2}
      accentColor={accentColor}
      onBack={onBack}
      onSkip={onSkip}
      heading="어떤 활동을 즐기세요?"
      subtitle="관심사를 선택하면 맞춤 활동을 추천해 드려요."
      footer={
        <PillButton onClick={onNext} color={accentColor}>
          다음
        </PillButton>
      }
    >
      <div className="mb-8 flex w-full flex-wrap gap-2.5">
        {PARENT_INTERESTS.map((interest) => {
          const selected = interests.includes(interest)
          return (
            <button
              key={interest}
              type="button"
              onClick={() => onToggleInterest(interest)}
              className="flex h-[42px] items-center rounded-full border px-[18px] text-[14px]"
              style={{
                backgroundColor: selected ? accentColor : '#f2f2ee',
                borderColor: selected ? accentColor : 'transparent',
                color: selected ? '#ffffff' : '#66695d',
                fontWeight: selected ? 600 : 400,
              }}
            >
              {interest}
            </button>
          )
        })}
      </div>
      <div className="flex w-full items-center gap-2 rounded-2xl bg-[#fff7cc] px-[18px] py-3.5">
        <span className="text-[13px]">💡</span>
        <span className="text-[13px] leading-[1.55] text-[#66695d]">나중에 설정에서 언제든 바꿀 수 있어요.</span>
      </div>
    </StepLayout>
  )
}
