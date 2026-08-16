import type { ReactNode } from 'react'
import Screen from '@/components/Screen'
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
    <Screen footer={footer}>
      <div className="bg-remine-bg">
        <StepNav onBack={onBack} onSkip={onSkip} />
        <div className="flex flex-col items-start px-6 pb-8 pt-8">
          <div className="w-full pb-8">
            <ProgressDots total={4} current={dotIndex} accentColor={accentColor} />
          </div>
          <h1 className="w-full text-[26px] font-semibold leading-[1.35] text-remine-dark">{heading}</h1>
          <p className="w-full pb-8 pt-1.5 text-[15px] leading-[1.5] text-remine-muted">{subtitle}</p>
          {children}
        </div>
      </div>
    </Screen>
  )
}
