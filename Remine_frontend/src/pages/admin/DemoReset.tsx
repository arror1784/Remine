import { useState } from 'react'
import Screen from '@/components/Screen'
import { resetDemoData, type DemoVariant } from '@/api/auth'
import { COLORS } from '@/theme'

type Status = 'idle' | 'loading' | 'success' | 'error'

// Internal utility page, deliberately not linked from any nav/menu — visit
// directly at /admin/demo-reset. Each button only ever touches its own
// account pair (see backend DemoResetService); EVAL and DEMO never affect
// each other.
export default function DemoReset() {
  const [demoStatus, setDemoStatus] = useState<Status>('idle')
  const [evalStatus, setEvalStatus] = useState<Status>('idle')

  const handleReset = async (variant: DemoVariant, setStatus: (s: Status) => void) => {
    setStatus('loading')
    try {
      await resetDemoData(variant)
      setStatus('success')
    } catch {
      setStatus('error')
    }
  }

  return (
    <Screen>
      <div className="flex h-full flex-col items-center justify-center gap-10 px-6 text-center">
        <ResetSection
          title="시연 데이터 초기화"
          description={
            <>
              시연용(DEMO) 계정의 체크리스트·사진·가족 게시물·메시지를 모두 지우고
              <br />
              보기 좋은 오늘 데이터로 되돌립니다.
            </>
          }
          buttonLabel="시연용 초기화"
          status={demoStatus}
          onReset={() => handleReset('DEMO', setDemoStatus)}
          color={COLORS.pink}
        />

        <div className="h-px w-full max-w-[280px]" style={{ backgroundColor: COLORS.border }} />

        <ResetSection
          title="심사 데이터 초기화"
          description={
            <>
              심사용(EVAL) 계정의 체크리스트·사진·가족 게시물·메시지를 모두 지우고
              <br />
              보기 좋은 오늘 데이터로 되돌립니다.
            </>
          }
          buttonLabel="심사용 초기화"
          status={evalStatus}
          onReset={() => handleReset('EVAL', setEvalStatus)}
          color={COLORS.blue}
        />
      </div>
    </Screen>
  )
}

function ResetSection({
  title,
  description,
  buttonLabel,
  status,
  onReset,
  color,
}: {
  title: string
  description: React.ReactNode
  buttonLabel: string
  status: Status
  onReset: () => void
  color: string
}) {
  return (
    <div className="flex flex-col items-center gap-4">
      <div className="flex flex-col gap-1">
        <h1 className="text-[20px] font-semibold text-remine-dark">{title}</h1>
        <p className="text-[13px] text-remine-muted">{description}</p>
      </div>

      <button
        type="button"
        onClick={onReset}
        disabled={status === 'loading'}
        className="h-[52px] w-full max-w-[280px] rounded-2xl text-[16px] font-semibold text-white"
        style={{ backgroundColor: status === 'loading' ? COLORS.muted : color }}
      >
        {status === 'loading' ? '초기화 중...' : buttonLabel}
      </button>

      {status === 'success' && (
        <p className="text-[14px] font-medium text-remine-dark">초기화 완료했어요.</p>
      )}
      {status === 'error' && (
        <p className="text-[14px] font-medium" style={{ color: COLORS.danger }}>
          초기화에 실패했어요. 잠시 후 다시 시도해 주세요.
        </p>
      )}
    </div>
  )
}
