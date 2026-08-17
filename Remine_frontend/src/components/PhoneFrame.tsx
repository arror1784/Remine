import type { ReactNode } from 'react'
import StatusBar from '@/components/StatusBar'

type PhoneFrameProps = {
  children: ReactNode
}

// Below the `sm` breakpoint this is a plain, unstyled full-viewport-height
// container — the device itself is the "frame". At `sm` and up (viewing on
// a wider screen) it renders a smartphone bezel around a fixed-size box,
// for previewing/demoing the mobile app. In both cases the box has a real,
// bounded height (not just a minimum), so page content (via <Screen>) can
// fill it exactly and scroll internally instead of relying on
// `position: fixed`, which some mobile browsers shift during scroll.
export default function PhoneFrame({ children }: PhoneFrameProps) {
  return (
    <div className="sm:flex sm:h-dvh sm:items-center sm:justify-center sm:bg-remine-surfaceSoft3 sm:p-10">
      <div className="relative mx-auto flex h-dvh w-full flex-col overflow-hidden bg-remine-bg sm:h-[min(852px,100%)] sm:w-[393px] sm:rounded-[54px] sm:border-[12px] sm:border-remine-nearBlack sm:shadow-[0_30px_70px_rgba(0,0,0,0.3)]">
        <div className="pointer-events-none absolute -left-[14px] top-[112px] hidden h-[26px] w-[3px] rounded-l bg-remine-nearBlack sm:block" />
        <div className="pointer-events-none absolute -left-[14px] top-[152px] hidden h-[44px] w-[3px] rounded-l bg-remine-nearBlack sm:block" />
        <div className="pointer-events-none absolute -left-[14px] top-[204px] hidden h-[44px] w-[3px] rounded-l bg-remine-nearBlack sm:block" />
        <div className="pointer-events-none absolute -right-[14px] top-[160px] hidden h-[70px] w-[3px] rounded-r bg-remine-nearBlack sm:block" />

        {/* Its own row in normal flow, not an overlay — page content below
            starts after it and never scrolls underneath it. */}
        <StatusBar />

        <div className="relative min-h-0 flex-1">{children}</div>

        <div className="pointer-events-none absolute bottom-1.5 left-1/2 z-40 hidden h-[5px] w-[134px] -translate-x-1/2 rounded-full bg-black/60 sm:block" />
      </div>
    </div>
  )
}
