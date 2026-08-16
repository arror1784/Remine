import StepLayout from '@/components/StepLayout'
import PillButton from '@/components/PillButton'
import type { Role } from '@/pages/onboarding/types'
import { ROLE_COLOR } from '@/pages/onboarding/types'
import checkSmall from '@/assets/check-small.svg'
import { COLORS } from '@/theme'

type RoleSelectStepProps = {
  role: Role | null
  onSelectRole: (role: Role) => void
  onBack: () => void
  onNext: () => void
}

const ROLE_CARDS: { role: Role; emoji: string; title: string; subtitle: string; desc: string }[] = [
  { role: 'parent', emoji: '👩', title: '부모님', subtitle: '60대 이상 어르신', desc: '건강 활동, 추억 퀴즈, 가족과 소통' },
  { role: 'child', emoji: '👧', title: '자녀', subtitle: '부모님을 케어하는 가족', desc: '부모님 상태 확인, 응원 메시지, 사진 추가' },
]

export default function RoleSelectStep({ role, onSelectRole, onBack, onNext }: RoleSelectStepProps) {
  const accentColor = role ? ROLE_COLOR[role] : COLORS.pink

  return (
    <StepLayout
      dotIndex={0}
      accentColor={accentColor}
      onBack={onBack}
      heading="누구로 시작할까요?"
      subtitle="나의 역할에 맞게 화면을 구성해 드려요."
      footer={
        <PillButton onClick={onNext} disabled={!role} color={accentColor}>
          다음
        </PillButton>
      }
    >
      <div className="flex w-full flex-col gap-4">
        {ROLE_CARDS.map((card) => {
          const selected = role === card.role
          const color = ROLE_COLOR[card.role]
          return (
            <button
              key={card.role}
              type="button"
              onClick={() => onSelectRole(card.role)}
              className="flex w-full flex-col gap-2.5 rounded-[20px] border-2 p-5 text-left"
              style={{
                borderColor: selected ? color : COLORS.border,
                backgroundColor: selected ? `${color}08` : COLORS.white,
              }}
            >
              <div className="flex w-full items-center gap-3.5">
                <div
                  className="flex size-[52px] items-center justify-center rounded-2xl text-xl"
                  style={{ backgroundColor: `${color}17` }}
                >
                  {card.emoji}
                </div>
                <div className="flex flex-col">
                  <div className="flex items-center gap-2">
                    <span className="text-[18px] font-semibold leading-[1.5] text-remine-dark">{card.title}</span>
                    {selected && (
                      <span
                        className="flex size-[18px] items-center justify-center rounded-full"
                        style={{ backgroundColor: color }}
                      >
                        <img src={checkSmall} alt="" className="size-[9px]" />
                      </span>
                    )}
                  </div>
                  <span className="text-[13px] leading-[1.5] text-remine-muted">{card.subtitle}</span>
                </div>
              </div>
              <p className="text-[14px] leading-[1.5] text-remine-subtle">{card.desc}</p>
            </button>
          )
        })}
      </div>
    </StepLayout>
  )
}
