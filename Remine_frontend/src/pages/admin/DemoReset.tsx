import { useState } from 'react'
import Screen from '@/components/Screen'
import { resetDemoData } from '@/api/auth'
import { COLORS } from '@/theme'

type Status = 'idle' | 'loading' | 'success' | 'error'

// Internal utility page, deliberately not linked from any nav/menu — visit
// directly at /admin/demo-reset. Only ever touches the DEMO account pair
// (see backend DemoResetService); the EVAL account used for AI product
// review is never affected.
export default function DemoReset() {
  const [status, setStatus] = useState<Status>('idle')

  const handleReset = async () => {
    setStatus('loading')
    try {
      await resetDemoData()
      setStatus('success')
    } catch {
      setStatus('error')
    }
  }

  return (
    <Screen>
      <div className="flex h-full flex-col items-center justify-center gap-6 px-6 text-center">
        <div className="flex flex-col gap-1">
          <h1 className="text-[20px] font-semibold text-remine-dark">시연 데이터 초기화</h1>
          <p className="text-[13px] text-remine-muted">
            시연용(DEMO) 계정의 체크리스트·사진·가족 게시물·메시지를 모두 지우고
            <br />
            보기 좋은 오늘 데이터로 되돌립니다. 심사용(EVAL) 계정은 영향받지 않습니다.
          </p>
        </div>

        <button
          type="button"
          onClick={handleReset}
          disabled={status === 'loading'}
          className="h-[52px] w-full max-w-[280px] rounded-2xl text-[16px] font-semibold text-white"
          style={{ backgroundColor: status === 'loading' ? COLORS.muted : COLORS.pink }}
        >
          {status === 'loading' ? '초기화 중...' : '지금 초기화'}
        </button>

        {status === 'success' && (
          <p className="text-[14px] font-medium text-remine-dark">초기화 완료했어요. 이제 시연을 시작하세요.</p>
        )}
        {status === 'error' && (
          <p className="text-[14px] font-medium" style={{ color: COLORS.danger }}>
            초기화에 실패했어요. 잠시 후 다시 시도해 주세요.
          </p>
        )}
      </div>
    </Screen>
  )
}
