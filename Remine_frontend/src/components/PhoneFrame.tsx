import type { ReactNode } from 'react'

type PhoneFrameProps = {
  children: ReactNode
}

// Below the `sm` breakpoint this renders as a plain, unstyled full-width
// container — the device itself is the "frame". At `sm` and up (viewing on
// a wider screen) it renders a smartphone bezel around a fixed-size,
// independently scrolling "screen", so `position: fixed` descendants
// (bottom nav, sticky CTAs) pin to that screen instead of the real
// viewport — the `transform` below is what makes it a containing block.
export default function PhoneFrame({ children }: PhoneFrameProps) {
  return (
    <div className="sm:flex sm:min-h-screen sm:items-center sm:justify-center sm:bg-[#e7e7e2] sm:py-10">
      <div className="relative mx-auto w-full bg-remine-bg sm:h-[852px] sm:w-[393px] sm:overflow-x-hidden sm:overflow-y-auto sm:rounded-[54px] sm:border-[12px] sm:border-[#111214] sm:shadow-[0_30px_70px_rgba(0,0,0,0.3)] sm:[transform:translateZ(0)]">
        <div className="pointer-events-none fixed left-1/2 top-0 z-30 hidden h-[30px] w-[126px] -translate-x-1/2 rounded-b-[18px] bg-[#111214] sm:block" />
        <div className="pointer-events-none fixed -left-[14px] top-[112px] hidden h-[26px] w-[3px] rounded-l bg-[#111214] sm:block" />
        <div className="pointer-events-none fixed -left-[14px] top-[152px] hidden h-[44px] w-[3px] rounded-l bg-[#111214] sm:block" />
        <div className="pointer-events-none fixed -left-[14px] top-[204px] hidden h-[44px] w-[3px] rounded-l bg-[#111214] sm:block" />
        <div className="pointer-events-none fixed -right-[14px] top-[160px] hidden h-[70px] w-[3px] rounded-r bg-[#111214] sm:block" />

        {children}

        <div className="pointer-events-none fixed bottom-1.5 left-1/2 z-40 hidden h-[5px] w-[134px] -translate-x-1/2 rounded-full bg-black/60 sm:block" />
      </div>
    </div>
  )
}
