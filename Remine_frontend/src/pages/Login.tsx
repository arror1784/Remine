import { useNavigate } from 'react-router-dom'
import Screen from '@/components/Screen'
import { useAuthStore } from '@/store/auth'
import type { Role } from '@/store/auth'

const PROFILES = [
  { role: 'parent', label: '부모로 보기', color: '#ff42ad', emoji: '👩', desc: '오늘의 건강, 활동 분석, 추억 퀴즈' },
  { role: 'child', label: '자녀로 보기', color: '#37ceff', emoji: '👧', desc: '부모님 상태 모니터링, 사진 추가, 메시지' },
] as const

export default function Login() {
  const navigate = useNavigate()
  const setActiveRole = useAuthStore((s) => s.setActiveRole)

  const enter = (role: Role) => {
    setActiveRole(role)
    navigate(`/${role}/home`)
  }

  return (
    <Screen>
      <div className="flex h-full flex-col justify-center gap-6 bg-remine-bg px-6">
        <div className="flex flex-col gap-1">
          <h1 className="text-[24px] font-semibold text-[#1a1a1a]">어떤 화면을 볼까요?</h1>
          <p className="text-[14px] text-[#9a9c91]">데모 계정으로 바로 둘러볼 수 있어요.</p>
        </div>

        <div className="flex flex-col gap-3">
          {PROFILES.map((p) => (
            <button
              key={p.role}
              type="button"
              onClick={() => enter(p.role)}
              className="flex items-center gap-3.5 rounded-2xl border-2 border-[#ebebeb] bg-white p-4 text-left"
            >
              <div className="flex size-12 items-center justify-center rounded-full bg-[#fff7cc] text-xl">{p.emoji}</div>
              <div className="flex-1">
                <span className="text-[16px] font-semibold" style={{ color: p.color }}>
                  {p.label}
                </span>
                <p className="pt-0.5 text-[13px] text-[#9a9c91]">{p.desc}</p>
              </div>
            </button>
          ))}
        </div>

        <button
          type="button"
          onClick={() => navigate('/onboarding')}
          className="text-[14px] font-semibold text-[#9a9c91] underline"
        >
          새 계정 만들기
        </button>
      </div>
    </Screen>
  )
}
