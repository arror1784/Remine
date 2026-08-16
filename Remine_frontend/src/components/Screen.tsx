import type { ReactNode } from 'react'

type ScreenProps = {
  children: ReactNode
  footer?: ReactNode
}

// A screen that fills its container height exactly (the real viewport on
// mobile, the device box inside PhoneFrame on desktop) and never itself
// scrolls — only the content area does. The footer (bottom nav, sticky
// CTA) sits in normal flow after it, so it's always on-screen without
// relying on `position: fixed`, which some mobile browsers shift during
// scroll/address-bar changes.
export default function Screen({ children, footer }: ScreenProps) {
  return (
    <div className="flex h-full flex-col">
      <div className="no-scrollbar flex-1 overflow-y-auto">{children}</div>
      {footer}
    </div>
  )
}
