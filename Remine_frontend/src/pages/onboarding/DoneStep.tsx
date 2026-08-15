import StepNav from '@/components/StepNav'
import PillButton from '@/components/PillButton'
import type { OnboardingState } from '@/pages/onboarding/types'
import { ROLE_COLOR } from '@/pages/onboarding/types'

type DoneStepProps = {
  state: OnboardingState
  onBack: () => void
  onFinish: () => void
}

export default function DoneStep({ state, onBack, onFinish }: DoneStepProps) {
  const { role, name, ageGroup, interests, inviteCode } = state
  if (!role) return null
  const accentColor = ROLE_COLOR[role]

  return (
    <div className="relative h-full min-h-[874px] w-full bg-remine-bg">
      <StepNav onBack={onBack} />
      <div className="flex flex-col items-center px-6 pt-8 text-center">
        <div
          className="mb-6 flex size-20 items-center justify-center rounded-[28px] text-3xl"
          style={{ backgroundColor: `${accentColor}17` }}
        >
          🎉
        </div>
        <h1 className="mb-2.5 text-[28px] font-semibold leading-[1.5] text-[#1a1a1a]">
          {role === 'parent' ? '시작할 준비가 됐어요!' : '연결 완료!'}
        </h1>
        <p className="mb-8 text-[15px] leading-[1.6] text-[#9a9c91]">
          {role === 'parent' ? 'REMIND와 함께 건강한 하루를 만들어 가요.' : `${name || '부모님'}과 연결됐어요. 이제 언제든 상태를 확인할 수 있어요.`}
        </p>

        {role === 'parent' ? (
          <div className="w-full rounded-3xl border border-[#ebebeb] bg-white p-5 text-left">
            <div className="mb-4 flex items-center gap-3.5">
              <div
                className="flex size-[50px] items-center justify-center rounded-2xl text-xl"
                style={{ backgroundColor: `${accentColor}17` }}
              >
                👩
              </div>
              <div>
                <p className="text-[18px] font-semibold leading-[1.5] text-[#1a1a1a]">{name || '회원'}님</p>
                <p className="text-[13px] leading-[1.5] text-[#9a9c91]">부모님 모드 · {ageGroup}</p>
              </div>
            </div>
            <div className="flex flex-wrap gap-2">
              {interests.map((interest) => (
                <span
                  key={interest}
                  className="flex h-[30px] items-center rounded-full px-3 text-[12px] font-semibold"
                  style={{ backgroundColor: `${accentColor}12`, color: accentColor }}
                >
                  {interest}
                </span>
              ))}
            </div>
          </div>
        ) : (
          <div className="relative w-full overflow-hidden rounded-3xl bg-[#1c1c1c] p-5 text-left">
            <div className="mb-3.5 flex items-center gap-3.5">
              <div
                className="flex size-[50px] items-center justify-center rounded-full border-2"
                style={{ backgroundColor: '#fff7cc', borderColor: accentColor }}
              >
                👩
              </div>
              <div className="flex-1">
                <p className="text-[17px] font-semibold leading-[1.5] text-white">{name || '부모님'}님</p>
                <p className="text-[13px] leading-[1.5] text-white/45">{inviteCode || '연결된 계정'}</p>
              </div>
              <span
                className="flex h-[34px] items-center rounded-full px-3 text-[12px] font-semibold"
                style={{ backgroundColor: `${accentColor}22`, color: accentColor }}
              >
                연결됨
              </span>
            </div>
            <div className="flex gap-2">
              {['상태 알림', '메시지', '퀴즈 사진'].map((tag) => (
                <span key={tag} className="flex h-7 items-center rounded-full bg-white/8 px-2.5 text-[12px] font-semibold text-white/60">
                  {tag}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>

      <PillButton onClick={onFinish} color={accentColor}>
        시작하기
      </PillButton>
    </div>
  )
}
