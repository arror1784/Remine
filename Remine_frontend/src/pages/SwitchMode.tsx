import { useLocation, useNavigate } from 'react-router-dom'
import type { Location } from 'react-router-dom'
import BottomSheet from '@/components/BottomSheet'

const PROFILES = [
  { role: 'parent', name: '윤정아님', badge: '부모님 모드', color: '#ff42ad', emoji: '👩', desc: '오늘의 건강, 활동 분석, 추억 퀴즈' },
  { role: 'child', name: '딸 지영님', badge: '자녀 모드', color: '#37ceff', emoji: '👧', desc: '부모님 상태 모니터링, 사진 추가, 메시지' },
] as const

export default function SwitchMode() {
  const navigate = useNavigate()
  const location = useLocation()
  const backgroundLocation = (location.state as { backgroundLocation?: Location } | null)?.backgroundLocation
  const originPathname = backgroundLocation?.pathname ?? window.location.pathname
  const current: string = originPathname.startsWith('/child') ? 'child' : 'parent'

  return (
    <BottomSheet>
      <div className="flex flex-col gap-1">
        <h2 className="text-[21px] font-semibold text-[#1a1a1a]">누가 사용하고 있나요?</h2>
        <p className="text-[14px] text-[#9a9c91]">모드에 따라 화면 구성이 달라져요.</p>
      </div>
      <div className="flex flex-col gap-3">
        {PROFILES.map((p) => {
          const selected = p.role === current
          return (
            <button
              key={p.role}
              type="button"
              onClick={() => navigate(`/${p.role}/home`)}
              className="flex items-center gap-3.5 rounded-2xl border-2 p-4 text-left"
              style={{ borderColor: selected ? p.color : '#ebebeb', backgroundColor: selected ? `${p.color}08` : '#ffffff' }}
            >
              <div className="flex size-12 items-center justify-center rounded-full bg-[#fff7cc] text-xl">{p.emoji}</div>
              <div className="flex-1">
                <div className="flex items-center gap-2">
                  <span className="text-[16px] font-semibold text-[#1a1a1a]">{p.name}</span>
                  <span className="rounded-full px-2.5 py-0.5 text-[12px] font-semibold" style={{ backgroundColor: `${p.color}17`, color: p.color }}>
                    {p.badge}
                  </span>
                </div>
                <p className="pt-0.5 text-[13px] text-[#9a9c91]">{p.desc}</p>
              </div>
              {selected && (
                <span className="flex size-6 shrink-0 items-center justify-center rounded-full text-white" style={{ backgroundColor: p.color }}>
                  ✓
                </span>
              )}
            </button>
          )
        })}
      </div>
    </BottomSheet>
  )
}
