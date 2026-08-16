import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import BottomSheet from '@/components/BottomSheet'

export default function AddMemoryPhoto() {
  const navigate = useNavigate()
  const [title, setTitle] = useState('')
  const [when, setWhen] = useState('')

  return (
    <BottomSheet>
      <div className="flex items-center justify-between">
        <h1 className="text-[20px] font-semibold text-remine-dark">추억 사진 추가하기</h1>
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="flex size-9 items-center justify-center rounded-full bg-remine-border text-remine-subtle"
        >
          ✕
        </button>
      </div>

      <div className="flex flex-col gap-5">
        <button
          type="button"
          className="flex flex-col items-center gap-1.5 rounded-2xl border-2 border-dashed border-remine-pink bg-remine-highlight py-8"
        >
          <span className="text-2xl">📷</span>
          <span className="text-[16px] font-semibold text-remine-dark">사진 선택하기</span>
          <span className="text-[13px] text-remine-subtle">어머니 퀴즈에 자동으로 활용돼요!</span>
        </button>

        <div className="flex flex-col gap-2">
          <label className="text-[14px] text-remine-dark">어떤 추억인가요?</label>
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="예) 속초 여행, 첫 손주 돌잔치..."
            className="h-14 rounded-2xl border border-remine-border bg-remine-surfaceSoft px-4 text-[16px] placeholder:text-remine-muted focus:outline-none"
          />
        </div>

        <div className="flex flex-col gap-2">
          <label className="text-[14px] text-remine-dark">언제의 사진인가요?</label>
          <input
            value={when}
            onChange={(e) => setWhen(e.target.value)}
            placeholder="예) 2022년 여름"
            className="h-14 rounded-2xl border border-remine-border bg-remine-surfaceSoft px-4 text-[16px] placeholder:text-remine-muted focus:outline-none"
          />
        </div>

        <button
          type="button"
          onClick={() => navigate('/child/memories')}
          disabled={!title || !when}
          className="h-[52px] rounded-2xl bg-remine-blue text-[16px] font-semibold text-white disabled:opacity-40"
        >
          어머니께 전달하기
        </button>
      </div>
    </BottomSheet>
  )
}

