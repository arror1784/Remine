import { useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import WelcomeStep from '@/pages/onboarding/WelcomeStep'
import RoleSelectStep from '@/pages/onboarding/RoleSelectStep'
import ProfileStep from '@/pages/onboarding/ProfileStep'
import DetailStep from '@/pages/onboarding/DetailStep'
import DoneStep from '@/pages/onboarding/DoneStep'
import type { OnboardingState, Role } from '@/pages/onboarding/types'
import { PAIRING_FAILED } from '@/pages/onboarding/types'
import { pairWithInviteCode, signUp } from '@/api/auth'
import { useAuthStore } from '@/store/auth'

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

  const accountRef = useRef<{ userId: string; accessToken: string } | null>(null)
  const failedInviteCodeRef = useRef<string | null>(null)

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

  const finish = async () => {
    const role = state.role
    if (!role) return

    // The account survives a failed pairing, so never sign up twice on retry.
    if (!accountRef.current) {
      accountRef.current = await signUp({
        role,
        name: state.name,
        ageGroup: state.ageGroup ?? '기타',
        interests: state.interests,
      })
    }
    const { userId, accessToken } = accountRef.current
    const { setSession, setActiveRole } = useAuthStore.getState()
    setSession(role, { userId, accessToken, pairedUserId: null })
    setActiveRole(role)

    const inviteCode = state.inviteCode.trim()
    if (role === 'child' && inviteCode && failedInviteCodeRef.current !== inviteCode) {
      try {
        const pairing = await pairWithInviteCode(inviteCode)
        setSession(role, {
          userId,
          accessToken: pairing.accessToken,
          pairedUserId: pairing.parentUserId,
        })
      } catch {
        // Retrying with the same code skips pairing and enters the app unpaired.
        failedInviteCodeRef.current = inviteCode
        throw new Error(PAIRING_FAILED)
      }
    }
    navigate(`/${role}/home`)
  }

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
      return <DoneStep state={state} onBack={goBack} onFinish={finish} />
    default:
      return null
  }
}
