import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Screen from '@/components/Screen'
import { COLORS } from '@/theme'

const MONTH_STATS = [
  { value: '4장', label: '추가한 사진' },
  { value: '12개', label: '보낸 메시지' },
  { value: '3회', label: '전화 통화' },
]

const ACCOUNT_ITEMS = [
  { title: '부모님 초대 코드 확인', value: 'REMIND-J7YQ' },
  { title: '다른 가족 초대하기' },
  { title: '연결된 부모님 변경' },
]

const INFO_ITEMS = [
  { title: '앱 버전', value: 'v1.0.0' },
  { title: '개인정보 처리방침' },
  { title: '서비스 이용약관' },
]

export default function ChildMyPage() {
  const navigate = useNavigate()
  const [alerts, setAlerts] = useState({ status: true, message: true, weekly: false })

  return (
    <Screen>
      <div className="flex items-center justify-between px-5 pt-[max(30px,env(safe-area-inset-top))]">
        <h1 className="text-[22px] font-semibold text-remine-dark">마이페이지</h1>
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="flex size-9 items-center justify-center rounded-full bg-remine-border text-remine-subtle"
        >
          ✕
        </button>
      </div>

      <div className="flex flex-col gap-6 px-5 pb-10 pt-5">
        <div className="rounded-3xl bg-remine-surfaceDark p-6">
          <div className="flex items-center gap-3.5">
            <div className="flex size-14 items-center justify-center rounded-full bg-remine-highlight text-xl">👧</div>
            <div>
              <p className="flex items-center gap-1.5 text-[19px] font-semibold text-white">김지영님 ✏️</p>
              <p className="flex items-center gap-1.5 pt-0.5 text-[13px] text-white/45">
                <span className="size-1.5 rounded-full bg-remine-blue" /> 자녀 모드 · 딸
              </p>
            </div>
          </div>
          <div className="mt-5 flex items-center gap-3 rounded-2xl bg-white/8 px-4 py-3">
            <span className="flex size-9 items-center justify-center rounded-full bg-remine-highlight text-[14px]">👩</span>
            <div className="flex-1">
              <p className="text-[14px] font-medium text-white">윤정아님 연결됨</p>
              <p className="text-[12px] text-white/40">어머니 · REMIND-W2KF</p>
            </div>
            <span className="rounded-full bg-remine-blue/20 px-3 py-1 text-[12px] font-semibold text-remine-blue">연결됨</span>
          </div>
        </div>

        <div className="grid grid-cols-3 gap-2.5">
          {MONTH_STATS.map((s) => (
            <div key={s.label} className="flex flex-col items-center gap-1 rounded-2xl border border-remine-border bg-white py-4">
              <span className="text-[20px] font-semibold text-remine-dark">{s.value}</span>
              <span className="text-[12px] text-remine-muted">{s.label}</span>
            </div>
          ))}
        </div>

        <div className="flex flex-col overflow-hidden rounded-[20px] bg-white">
          <p className="px-5 pb-2 pt-4 text-[12px] font-semibold tracking-wide text-remine-muted">알림 설정</p>
          {[
            { key: 'status' as const, label: '어머니 상태 변화 알림' },
            { key: 'message' as const, label: '가족 메시지 알림' },
            { key: 'weekly' as const, label: '주간 요약 리포트' },
          ].map((row, i) => (
            <div key={row.key} className={`flex items-center justify-between px-5 py-3.5 ${i > 0 ? 'border-t border-remine-surfaceAlt' : ''}`}>
              <span className="text-[16px] text-remine-dark">{row.label}</span>
              <button
                type="button"
                onClick={() => setAlerts((a) => ({ ...a, [row.key]: !a[row.key] }))}
                className="flex h-7 w-12 items-center rounded-full px-1 transition-colors"
                style={{ backgroundColor: alerts[row.key] ? COLORS.blue : COLORS.border }}
              >
                <span
                  className="size-5 rounded-full bg-white shadow transition-transform"
                  style={{ transform: alerts[row.key] ? 'translateX(20px)' : 'translateX(0)' }}
                />
              </button>
            </div>
          ))}
        </div>

        <div className="flex flex-col overflow-hidden rounded-[20px] bg-white">
          <p className="px-5 pb-2 pt-4 text-[12px] font-semibold tracking-wide text-remine-muted">계정</p>
          {ACCOUNT_ITEMS.map((item, i) => (
            <div key={item.title} className={`flex items-center justify-between px-5 py-3.5 ${i > 0 ? 'border-t border-remine-surfaceAlt' : ''}`}>
              <span className="text-[16px] font-medium text-remine-dark">{item.title}</span>
              {item.value ? <span className="text-[14px] text-remine-muted">{item.value}</span> : <span className="text-remine-offline">›</span>}
            </div>
          ))}
        </div>

        <div className="flex flex-col overflow-hidden rounded-[20px] bg-white">
          <p className="px-5 pb-2 pt-4 text-[12px] font-semibold tracking-wide text-remine-muted">앱 정보</p>
          {INFO_ITEMS.map((item, i) => (
            <div key={item.title} className={`flex items-center justify-between px-5 py-3.5 ${i > 0 ? 'border-t border-remine-surfaceAlt' : ''}`}>
              <span className="text-[16px] font-medium text-remine-dark">{item.title}</span>
              {item.value ? <span className="text-[14px] text-remine-muted">{item.value}</span> : <span className="text-remine-offline">›</span>}
            </div>
          ))}
        </div>

        <button type="button" className="h-[52px] rounded-2xl border border-remine-border bg-white text-[16px] font-semibold text-remine-danger">
          로그아웃
        </button>
      </div>
    </Screen>
  )
}
