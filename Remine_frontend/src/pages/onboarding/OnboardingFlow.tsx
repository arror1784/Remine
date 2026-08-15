import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import WelcomeStep from '@/pages/onboarding/WelcomeStep'
import RoleSelectStep from '@/pages/onboarding/RoleSelectStep'
import ProfileStep from '@/pages/onboarding/ProfileStep'
import DetailStep from '@/pages/onboarding/DetailStep'
import DoneStep from '@/pages/onboarding/DoneStep'
import type { OnboardingState, Role } from '@/pages/onboarding/types'

const STEP_COUNT = 5

export default function OnboardingFlow() {
  const navigate = useNavigate()
  const [step, setStep] = useState(0)
  const [state, setState] = useState<OnboardingState>({
    role: null,
    name: '',
    ageGroup: null,
    interests: [],
    inviteCode: '',
  })

  const goNext = () => setStep((s) => Math.min(s + 1, STEP_COUNT - 1))
  const goBack = () => setStep((s) => Math.max(s - 1, 0))

  const toggleInterest = (interest: string) =>
    setState((s) => ({
      ...s,
      interests: s.interests.includes(interest)
        ? s.interests.filter((i) => i !== interest)
        : [...s.interests, interest],
    }))

  const selectRole = (role: Role) => setState((s) => ({ ...s, role }))

  switch (step) {
    case 0:
      return <WelcomeStep onNext={goNext} />
    case 1:
      return <RoleSelectStep role={state.role} onSelectRole={selectRole} onBack={goBack} onNext={goNext} />
    case 2:
      if (!state.role) return null
      return (
        <ProfileStep
          role={state.role}
          name={state.name}
          ageGroup={state.ageGroup}
          onChangeName={(name) => setState((s) => ({ ...s, name }))}
          onSelectAgeGroup={(ageGroup) => setState((s) => ({ ...s, ageGroup }))}
          onBack={goBack}
          onSkip={goNext}
          onNext={goNext}
        />
      )
    case 3:
      if (!state.role) return null
      return (
        <DetailStep
          role={state.role}
          interests={state.interests}
          onToggleInterest={toggleInterest}
          inviteCode={state.inviteCode}
          onChangeInviteCode={(inviteCode) => setState((s) => ({ ...s, inviteCode }))}
          onBack={goBack}
          onSkip={state.role === 'parent' ? goNext : undefined}
          onNext={goNext}
        />
      )
    case 4:
      return <DoneStep state={state} onBack={goBack} onFinish={() => navigate(`/${state.role}/home`)} />
    default:
      return null
  }
}
