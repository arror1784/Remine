import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import logo from '@/assets/onboarding-logo.svg'
import remindIcon from '@/assets/onboarding-remind-icon.svg'

export default function Splash() {
  const navigate = useNavigate()

  useEffect(() => {
    const timer = setTimeout(() => navigate('/onboarding'), 1200)
    return () => clearTimeout(timer)
  }, [navigate])

  return (
    <div className="flex h-full min-h-[874px] w-full items-center justify-center bg-remine-bg">
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
