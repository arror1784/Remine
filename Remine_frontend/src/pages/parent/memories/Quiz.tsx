import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Screen from '@/components/Screen'
import BottomTabBar from '@/components/BottomTabBar'
import familyTrip from '@/assets/memories/family-trip.png'

const QUESTIONS = [
  { question: '이 사진은 어느 계절에 찍으셨나요?', options: ['봄', '여름', '가을', '겨울'], answer: 0 },
  { question: '누구와 함께 하였나요?', options: ['자녀', '친구', '남편', '손자'], answer: 0 },
  { question: '이 날 가장 기억에 남았던 순간은?', options: ['예쁜 꽃들', '딸들과의 대화', '맛있는 음식', '손자의 걸음마'], answer: 1 },
]

export default function MemoryQuiz() {
  const navigate = useNavigate()
  const [step, setStep] = useState(0)
  const [correctCount, setCorrectCount] = useState(0)
  const done = step >= QUESTIONS.length

  const selectAnswer = (index: number) => {
    if (index === QUESTIONS[step].answer) setCorrectCount((c) => c + 1)
    setStep((s) => s + 1)
  }

  return (
    <Screen footer={!done && <BottomTabBar role="parent" accentColor="#ff42ad" />}>
      <div className="flex items-center gap-3 px-5 pt-5">
        {!done && (
          <button type="button" onClick={() => navigate('/parent/memories')} className="text-[20px] text-[#1a1a1a]">
            ‹
          </button>
        )}
        <h1 className="text-[20px] font-semibold text-[#1a1a1a]">추억 퀴즈</h1>
      </div>

      <div className="flex flex-col gap-5 px-5 pb-10 pt-5">
        <div className="h-[180px] w-full overflow-hidden rounded-2xl">
          <img src={familyTrip} alt="추억 사진" className="size-full object-cover" />
        </div>

        {done ? (
          <div className="flex flex-col items-center pt-4 text-center">
            <div className="mb-5 flex size-20 items-center justify-center rounded-[28px] bg-remine-pink/10 text-3xl">🎉</div>
            <h2 className="mb-2 text-[24px] font-semibold text-[#1a1a1a]">퀴즈 완료!</h2>
            <p className="mb-1 text-[16px] text-[#1a1a1a]">
              {QUESTIONS.length}문제 중 <span className="font-semibold text-remine-pink">{correctCount}문제</span> 맞혔어요!
            </p>
            <p className="mb-8 text-[14px] text-[#9a9c91]">소중한 추억을 떠올리는 시간이었어요</p>
            <button
              type="button"
              onClick={() => navigate('/parent/memories')}
              className="h-[52px] w-full rounded-2xl bg-[#1a1a1a] text-[16px] font-semibold text-white"
            >
              갤러리 돌아가기
            </button>
          </div>
        ) : (
          <>
            <div className="flex gap-1.5">
              {QUESTIONS.map((_, i) => (
                <div
                  key={i}
                  className="h-1 flex-1 rounded-full"
                  style={{ backgroundColor: i === step ? '#ff42ad' : '#ebebeb' }}
                />
              ))}
            </div>
            <p className="text-[14px] font-medium text-[#9a9c91]">
              {step + 1} / {QUESTIONS.length}
            </p>
            <h2 className="text-[20px] font-semibold leading-[1.4] text-[#1a1a1a]">{QUESTIONS[step].question}</h2>
            <div className="flex flex-col gap-2.5">
              {QUESTIONS[step].options.map((option, i) => (
                <button
                  key={option}
                  type="button"
                  onClick={() => selectAnswer(i)}
                  className="flex h-14 items-center gap-3 rounded-2xl bg-white px-4 text-left text-[16px] text-[#1a1a1a]"
                >
                  <span className="flex size-6 shrink-0 items-center justify-center rounded-full border border-[#ebebeb] text-[13px] text-[#9a9c91]">
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
