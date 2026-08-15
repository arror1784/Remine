import type { ReactNode } from 'react'

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
    <div className="sm:flex sm:min-h-screen sm:items-center sm:justify-center sm:bg-[#e7e7e2] sm:py-10">
      <div className="relative mx-auto h-dvh w-full overflow-hidden bg-remine-bg sm:h-[852px] sm:w-[393px] sm:rounded-[54px] sm:border-[12px] sm:border-[#111214] sm:shadow-[0_30px_70px_rgba(0,0,0,0.3)]">
        <div className="pointer-events-none absolute left-1/2 top-0 z-30 hidden h-[30px] w-[126px] -translate-x-1/2 rounded-b-[18px] bg-[#111214] sm:block" />
        <div className="pointer-events-none absolute -left-[14px] top-[112px] hidden h-[26px] w-[3px] rounded-l bg-[#111214] sm:block" />
        <div className="pointer-events-none absolute -left-[14px] top-[152px] hidden h-[44px] w-[3px] rounded-l bg-[#111214] sm:block" />
        <div className="pointer-events-none absolute -left-[14px] top-[204px] hidden h-[44px] w-[3px] rounded-l bg-[#111214] sm:block" />
        <div className="pointer-events-none absolute -right-[14px] top-[160px] hidden h-[70px] w-[3px] rounded-r bg-[#111214] sm:block" />

        {children}

        <div className="pointer-events-none absolute bottom-1.5 left-1/2 z-40 hidden h-[5px] w-[134px] -translate-x-1/2 rounded-full bg-black/60 sm:block" />
      </div>
    </div>
  )
}
