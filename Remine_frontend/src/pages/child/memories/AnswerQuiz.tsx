// 디자인 검토 전 — 기존 패턴 재사용한 임시 UI
import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import Screen from '@/components/Screen'
import ModeBar from '@/components/ModeBar'
import { completeQuizWithAnswers, getDraftQuestions } from '@/api/memory'
import type { DraftQuestion } from '@/api/memory'
import { COLORS } from '@/theme'

export default function AnswerQuiz() {
  const { photoId } = useParams<{ photoId: string }>()
  const navigate = useNavigate()
  const [questions, setQuestions] = useState<DraftQuestion[]>([])
  const [answers, setAnswers] = useState<Record<string, string>>({})
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!photoId) return
    let cancelled = false
    const load = async () => {
      try {
        const draft = await getDraftQuestions(photoId)
        if (!cancelled) setQuestions(draft)
      } catch {
        if (!cancelled) setFailed(true)
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => {
      cancelled = true
    }
  }, [photoId])

  const allAnswered = questions.length > 0 && questions.every((q) => (answers[q.id] ?? '').trim())

  const handleSubmit = async () => {
    if (!photoId) return
    setSubmitting(true)
    setError(null)
    try {
      await completeQuizWithAnswers(
        photoId,
        questions.map((q) => ({ questionId: q.id, answer: answers[q.id].trim() }))
      )
      navigate('/child/memories')
    } catch {
      setError('퀴즈를 완성하지 못했어요. 잠시 후 다시 시도해 주세요.')
      setSubmitting(false)
    }
  }

  return (
    <Screen
      footer={
        questions.length > 0 ? (
          <div className="flex flex-col gap-2 px-5 pb-5">
            {error && <p className="text-center text-[13px] text-remine-pink">{error}</p>}
            <button
              type="button"
              onClick={handleSubmit}
              disabled={!allAnswered || submitting}
              className="h-[52px] rounded-2xl bg-remine-blue text-[16px] font-semibold text-white disabled:opacity-40"
            >
              {submitting ? '완성 중...' : '퀴즈 완성하기'}
            </button>
          </div>
        ) : undefined
      }
    >
      <ModeBar label="자녀 모드" color={COLORS.blue} />

      <div className="flex items-center gap-2 px-5 py-3.5">
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="flex size-9 items-center justify-center rounded-full bg-remine-border text-remine-subtle"
        >
          ←
        </button>
        <h1 className="text-[22px] font-semibold text-remine-dark">정답 알려주기</h1>
      </div>

      <div className="flex flex-col gap-5 px-5 pb-10">
        {loading && <p className="py-10 text-center text-[15px] text-remine-muted">불러오는 중...</p>}

        {!loading && failed && <p className="py-10 text-center text-[15px] text-remine-muted">불러오지 못했어요</p>}

        {!loading && !failed && questions.length === 0 && (
          <div className="flex flex-col items-center gap-2 rounded-[20px] border border-remine-border bg-white px-5 py-10 text-center">
            <span className="text-3xl">🧩</span>
            <p className="text-[16px] font-semibold text-remine-dark">아직 만들어진 질문이 없어요</p>
            <p className="text-[14px] leading-[1.6] text-remine-muted">
              사진에서 질문을 만드는 데 실패했어요. 사진을 다시 추가해 주세요.
            </p>
          </div>
        )}

        {!loading && questions.length > 0 && (
          <>
            <div className="flex gap-3 rounded-[20px] bg-remine-highlightBlue px-5 py-4">
              <span className="text-[15px]">💡</span>
              <p className="text-[13px] leading-[1.5] text-remine-subtle">
                사진을 보고 만든 질문이에요. 실제 정답을 알려주시면 어머니 퀴즈로 완성돼요.
              </p>
            </div>

            {questions.map((q, i) => (
              <div key={q.id} className="flex flex-col gap-2.5 rounded-[20px] border border-remine-border bg-white p-4">
                <p className="text-[13px] font-semibold text-remine-blue">질문 {i + 1}</p>
                <p className="text-[16px] leading-[1.5] text-remine-dark">{q.question}</p>
                <label className="text-[14px] text-remine-muted">정답을 입력해주세요</label>
                <input
                  value={answers[q.id] ?? ''}
                  onChange={(e) => setAnswers((prev) => ({ ...prev, [q.id]: e.target.value }))}
                  placeholder="정답을 적어주세요"
                  className="h-14 rounded-2xl border border-remine-border bg-remine-surfaceSoft px-4 text-[16px] placeholder:text-remine-muted focus:outline-none"
                />
              </div>
            ))}
          </>
        )}
      </div>
    </Screen>
  )
}
