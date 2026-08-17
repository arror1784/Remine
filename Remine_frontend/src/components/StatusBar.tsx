import { useEffect, useState } from 'react'

function formatTime(date: Date) {
  const hours = date.getHours()
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const displayHours = hours % 12 === 0 ? 12 : hours % 12
  return `${displayHours}:${minutes}`
}

// Purely cosmetic device chrome for the desktop preview mockup (see
// PhoneFrame) — on an actual phone the browser IS the phone and shows its
// own real status bar, so this never renders below the `sm` breakpoint.
// A real row in PhoneFrame's flex layout, not an absolute overlay, so it
// always keeps its own space and app content never scrolls under it.
export default function StatusBar() {
  const [now, setNow] = useState(() => new Date())

  useEffect(() => {
    const id = setInterval(() => setNow(new Date()), 15000)
    return () => clearInterval(id)
  }, [])

  return (
    <div className="hidden h-11 shrink-0 items-center justify-between bg-remine-bg px-7 text-[15px] font-semibold tracking-tight text-remine-dark sm:flex">
      <span>{formatTime(now)}</span>
      <div className="flex items-center gap-1.5">
        <svg width="18" height="12" viewBox="0 0 18 12" fill="none" aria-hidden>
          <rect x="0" y="7" width="3" height="5" rx="0.5" fill="currentColor" />
          <rect x="5" y="5" width="3" height="7" rx="0.5" fill="currentColor" />
          <rect x="10" y="3" width="3" height="9" rx="0.5" fill="currentColor" />
          <rect x="15" y="0" width="3" height="12" rx="0.5" fill="currentColor" />
        </svg>
        <svg width="16" height="12" viewBox="0 0 16 12" fill="none" aria-hidden>
          <path
            d="M8 10.2c.66 0 1.2-.54 1.2-1.2S8.66 7.8 8 7.8 6.8 8.34 6.8 9s.54 1.2 1.2 1.2ZM8 6.3c1 0 1.9.36 2.62.97l1.06-1.27A6.98 6.98 0 0 0 8 4.3c-1.42 0-2.71.5-3.68 1.7l1.06 1.27A4.98 4.98 0 0 1 8 6.3Zm0-3.4c1.94 0 3.7.68 5.1 1.8l1.06-1.27A9.98 9.98 0 0 0 8 1a9.98 9.98 0 0 0-6.16 2.43l1.06 1.27A7.98 7.98 0 0 1 8 2.9Z"
            fill="currentColor"
          />
        </svg>
        <svg width="25" height="12" viewBox="0 0 25 12" fill="none" aria-hidden>
          <rect x="0.5" y="0.5" width="21" height="11" rx="2.5" stroke="currentColor" opacity="0.4" />
          <rect x="2" y="2" width="18" height="8" rx="1.5" fill="currentColor" />
          <rect x="22.5" y="4" width="1.5" height="4" rx="0.75" fill="currentColor" opacity="0.4" />
        </svg>
      </div>
    </div>
  )
}
