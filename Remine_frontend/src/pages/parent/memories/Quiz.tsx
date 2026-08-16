import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Screen from '@/components/Screen'
import BottomTabBar from '@/components/BottomTabBar'
import { getTodayQuiz, submitQuizAttempt, type TodayQuiz } from '@/api/memory'
import { COLORS } from '@/theme'

export default function MemoryQuiz() {
  const navigate = useNavigate()
  const [quiz, setQuiz] = useState<TodayQuiz | null>(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)

  const [step, setStep] = useState(0)
  const [answers, setAnswers] = useState<number[]>([])
  const [result, setResult] = useState<{ correctCount: number; totalCount: number } | null>(null)
  const [submitFailed, setSubmitFailed] = useState(false)

  useEffect(() => {
    let cancelled = false
    getTodayQuiz()
      .then((data) => {
        if (!cancelled) setQuiz(data)
      })
      .catch(() => {
        if (!cancelled) setFailed(true)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  const photo = quiz?.photo ?? null
  const questions = quiz?.questions ?? []
  const answered = photo !== null && questions.length > 0 && step >= questions.length

  const submit = async (finalAnswers: number[]) => {
    if (!photo) return
    setSubmitFailed(false)
    try {
      setResult(await submitQuizAttempt(photo.id, finalAnswers))
    } catch {
      setSubmitFailed(true)
    }
  }

  const selectAnswer = (index: number) => {
    const next = [...answers, index]
    setAnswers(next)
    setStep((s) => s + 1)
    if (next.length === questions.length) void submit(next)
  }

  const header = (
    <div className="flex items-center gap-3 px-5 pt-[max(30px,env(safe-area-inset-top))]">
      {!answered && (
        <button type="button" onClick={() => navigate('/parent/memories')} className="text-[20px] text-remine-dark">
          ‹
        </button>
      )}
      <h1 className="text-[20px] font-semibold text-remine-dark">추억 퀴즈</h1>
    </div>
  )

  if (loading || failed || !photo || questions.length === 0) {
    return (
      <Screen footer={<BottomTabBar role="parent" accentColor={COLORS.pink} />}>
        {header}
        <div className="px-5 pb-10 pt-5">
          {loading ? (
            <p className="py-10 text-center text-[15px] text-remine-muted">불러오는 중...</p>
          ) : (
            <div className="flex flex-col items-center pt-6 text-center">
              <p className="mb-8 text-[16px] text-remine-muted">
                {failed ? '불러오지 못했어요' : '오늘은 풀 수 있는 퀴즈가 없어요'}
              </p>
              <button
                type="button"
                onClick={() => navigate('/parent/memories')}
                className="h-[52px] w-full rounded-2xl bg-remine-dark text-[16px] font-semibold text-white"
              >
                갤러리 돌아가기
              </button>
            </div>
          )}
        </div>
      </Screen>
    )
  }

  return (
    <Screen footer={!answered && <BottomTabBar role="parent" accentColor={COLORS.pink} />}>
      {header}

      <div className="flex flex-col gap-5 px-5 pb-10 pt-5">
        <div className="h-[180px] w-full overflow-hidden rounded-2xl bg-remine-highlight">
          <img src={photo.photoUrl} alt={photo.title} className="size-full object-cover" />
        </div>

        {answered ? (
          <div className="flex flex-col items-center pt-4 text-center">
            <div className="mb-5 flex size-20 items-center justify-center rounded-[28px] bg-remine-pink/10 text-3xl">🎉</div>
            <h2 className="mb-2 text-[24px] font-semibold text-remine-dark">퀴즈 완료!</h2>
            {result ? (
              <p className="mb-1 text-[16px] text-remine-dark">
                {result.totalCount}문제 중 <span className="font-semibold text-remine-pink">{result.correctCount}문제</span> 맞혔어요!
              </p>
            ) : (
              <p className="mb-1 text-[16px] text-remine-dark">
                {submitFailed ? '채점 결과를 불러오지 못했어요' : '채점하고 있어요...'}
              </p>
            )}
            <p className="mb-8 text-[14px] text-remine-muted">소중한 추억을 떠올리는 시간이었어요</p>
            {submitFailed && !result && (
              <button
                type="button"
                onClick={() => void submit(answers)}
                className="mb-3 h-[52px] w-full rounded-2xl bg-remine-highlight text-[16px] font-semibold text-remine-dark"
              >
                다시 시도하기
              </button>
            )}
            <button
              type="button"
              onClick={() => navigate('/parent/memories')}
              className="h-[52px] w-full rounded-2xl bg-remine-dark text-[16px] font-semibold text-white"
            >
              갤러리 돌아가기
            </button>
          </div>
        ) : (
          <>
            <div className="flex gap-1.5">
              {questions.map((q, i) => (
                <div
                  key={q.id}
                  className="h-1 flex-1 rounded-full"
                  style={{ backgroundColor: i === step ? COLORS.pink : COLORS.border }}
                />
              ))}
            </div>
            <p className="text-[14px] font-medium text-remine-muted">
              {step + 1} / {questions.length}
            </p>
            <h2 className="text-[20px] font-semibold leading-[1.4] text-remine-dark">{questions[step].question}</h2>
            <div className="flex flex-col gap-2.5">
              {questions[step].options.map((option, i) => (
                <button
                  key={option}
                  type="button"
                  onClick={() => selectAnswer(i)}
                  className="flex h-14 items-center gap-3 rounded-2xl bg-white px-4 text-left text-[16px] text-remine-dark"
                >
                  <span className="flex size-6 shrink-0 items-center justify-center rounded-full border border-remine-border text-[13px] text-remine-muted">
                    {i + 1}
                  </span>
                  {option}
                </button>
              ))}
            </div>
          </>
        )}
      </div>
    </Screen>
  )
}
