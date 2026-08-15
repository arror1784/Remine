import type { ReactNode } from 'react'
import StepNav from '@/components/StepNav'
import ProgressDots from '@/components/ProgressDots'

type StepLayoutProps = {
  dotIndex: number
  accentColor: string
  onBack?: () => void
  onSkip?: () => void
  heading: string
  subtitle: string
  children: ReactNode
  footer: ReactNode
}

export default function StepLayout({
  dotIndex,
  accentColor,
  onBack,
  onSkip,
  heading,
  subtitle,
  children,
  footer,
}: StepLayoutProps) {
  return (
    <div className="relative min-h-screen w-full bg-remine-bg">
      <StepNav onBack={onBack} onSkip={onSkip} />
      <div className="flex flex-col items-start px-6 pb-32 pt-8">
        <div className="w-full pb-8">
          <ProgressDots total={4} current={dotIndex} accentColor={accentColor} />
        </div>
        <h1 className="w-full text-[26px] font-semibold leading-[1.35] text-[#1a1a1a]">{heading}</h1>
        <p className="w-full pb-8 pt-1.5 text-[15px] leading-[1.5] text-[#9a9c91]">{subtitle}</p>
        {children}
      </div>
      {footer}
    </div>
  )
}
