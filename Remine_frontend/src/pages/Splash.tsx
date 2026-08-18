import { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import logo from '@/assets/onboarding-logo.svg'
import remindIcon from '@/assets/onboarding-remind-icon.svg'
import { demoLogin } from '@/api/auth'
import { useAuthStore } from '@/store/auth'

export default function Splash() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  // No query param = EVAL (unchanged default, since this URL may already be
  // under AI product review). ?demo=1 opts into the separate DEMO account
  // pair so live demos never touch — or get dirtied by — the EVAL account.
  const variant = searchParams.get('demo') === '1' ? 'DEMO' : 'EVAL'

  useEffect(() => {
    let cancelled = false

    const timer = setTimeout(async () => {
      const { sessions, activeRole, setSession, setActiveRole } = useAuthStore.getState()

      // Cached-session reuse only applies to the default EVAL flow (byte-for-byte the
      // original behavior). ?demo=1 always logs into the DEMO pair fresh, so a device
      // that was previously used for EVAL (no param) never has its cached EVAL session
      // silently reused for a demo, or vice versa.
      if (variant === 'EVAL') {
        if (sessions[activeRole]) {
          navigate(`/${activeRole}/home`)
          return
        }
        if (sessions.parent || sessions.child) {
          const role = sessions.parent ? 'parent' : 'child'
          setActiveRole(role)
          navigate(`/${role}/home`)
          return
        }
      }

      try {
        const [parent, child] = await Promise.all([demoLogin('parent', variant), demoLogin('child', variant)])
        if (cancelled) return
        setSession('parent', parent)
        setSession('child', child)
        setActiveRole('parent')
        navigate('/parent/home')
      } catch {
        // Backend not reachable — the app still has to be usable standalone.
        if (!cancelled) navigate('/onboarding')
      }
    }, 1200)

    return () => {
      cancelled = true
      clearTimeout(timer)
    }
  }, [navigate, variant])

  return (
    <div className="flex h-full w-full items-center justify-center bg-remine-bg">
      <div className="relative inline-grid grid-cols-[max-content] grid-rows-[max-content] place-items-start">
        <div className="col-start-1 row-start-1 h-[42.533px] w-[137.96px]">
          <img alt="Remine" className="block h-full w-full" src={logo} />
        </div>
        <div className="col-start-1 row-start-1 ml-[135.45px] mt-[6.41px] flex h-[36.015px] w-[35.548px] items-center justify-center">
          <div className="size-[27.408px] -rotate-[25deg]">
            <img alt="" className="block h-full w-full" src={remindIcon} />
          </div>
        </div>
      </div>
    </div>
  )
}
