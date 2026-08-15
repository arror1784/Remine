import logo from '@/assets/onboarding-logo.svg'
import remindIcon from '@/assets/onboarding-remind-icon.svg'
import PillButton from '@/components/PillButton'

type WelcomeStepProps = {
  onNext: () => void
}

export default function WelcomeStep({ onNext }: WelcomeStepProps) {
  return (
    <div className="relative flex h-full min-h-[874px] w-full flex-col items-center justify-center bg-remine-bg px-6">
      <div className="absolute left-1/2 top-1/2 flex w-[245px] -translate-x-1/2 -translate-y-1/2 flex-col items-start gap-6">
        <div className="relative inline-grid grid-cols-[max-content] grid-rows-[max-content] place-items-start">
          <div className="col-start-1 row-start-1 h-[49.745px] w-[161.356px]">
            <img alt="Remine" className="block h-full w-full" src={logo} />
          </div>
          <div className="col-start-1 row-start-1 ml-[158.42px] mt-[7.5px] flex h-[42.123px] w-[41.577px] items-center justify-center">
            <div className="size-[30.926px] -rotate-[25deg]">
              <img alt="" className="block h-full w-full" src={remindIcon} />
            </div>
          </div>
        </div>
        <p className="whitespace-pre-wrap break-words font-semibold text-black">
          <span className="text-[32px] leading-[1.4]">오늘을 </span>
          <span className="text-[32px] leading-[1.4] text-remine-pink">기록</span>
          <span className="text-[32px] leading-[1.4]">하고, </span>
          <br />
          <span className="text-[32px] leading-[1.4]">내일을 </span>
          <span className="text-[32px] leading-[1.4] text-remine-blue">기억</span>
          <span className="text-[32px] leading-[1.4]">하다</span>
        </p>
      </div>

      <PillButton onClick={onNext}>시작하기</PillButton>
    </div>
  )
}
