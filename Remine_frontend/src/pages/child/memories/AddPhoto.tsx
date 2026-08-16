// 디자인 검토 전 — 기존 패턴 재사용한 임시 UI
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import BottomSheet from '@/components/BottomSheet'
import { generateQuizQuestions, uploadPhoto } from '@/api/memory'
import anniversary from '@/assets/memories/anniversary.jpg'
import birthdayCake from '@/assets/memories/birthday-cake.png'
import familyTrip from '@/assets/memories/family-trip.png'
import grandchildWalk from '@/assets/memories/grandchild-walk.png'
import grandchild from '@/assets/memories/grandchild.png'
import sokchoTrip from '@/assets/memories/sokcho-trip.png'

// 실제 파일 업로드/스토리지가 아직 없어서, 앱에 번들된 샘플 사진 중 하나를 골라
// 그 경로를 photoUrl로 보낸다. 동작하지 않는 가짜 file input 대신 둔 임시 장치.
const SAMPLE_PHOTOS = [
  { src: familyTrip, label: '가족 여행' },
  { src: birthdayCake, label: '생신' },
  { src: grandchildWalk, label: '손주와 산책' },
  { src: anniversary, label: '기념일' },
  { src: grandchild, label: '손주' },
  { src: sokchoTrip, label: '속초 여행' },
]

export default function AddMemoryPhoto() {
  const navigate = useNavigate()
  const [title, setTitle] = useState('')
  const [memoryLabel, setMemoryLabel] = useState('')
  const [photoUrl, setPhotoUrl] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async () => {
    setSubmitting(true)
    setError(null)
    let photoId: string
    try {
      photoId = (await uploadPhoto({ title, photoUrl, memoryLabel })).id
    } catch {
      setError('사진을 전달하지 못했어요. 잠시 후 다시 시도해 주세요.')
      setSubmitting(false)
      return
    }
    // 사진은 이미 저장됐으므로 질문 생성이 실패해도 되돌아가지 않는다. 여기서 막으면
    // 다시 누를 때 같은 사진이 중복 저장된다. 실패는 다음 화면의 빈 상태로 안내한다.
    try {
      await generateQuizQuestions(photoId)
    } catch {
      // 다음 화면에서 draft 질문을 다시 불러오며 빈 상태를 보여준다.
    }
    navigate(`/child/memories/${photoId}/answer-quiz`)
  }

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
        <div className="flex flex-col gap-2">
          <label className="text-[14px] text-remine-dark">사진을 골라주세요</label>
          <div className="no-scrollbar -mx-1 flex gap-2.5 overflow-x-auto px-1 pb-1">
            {SAMPLE_PHOTOS.map((sample) => (
              <button
                key={sample.src}
                type="button"
                onClick={() => setPhotoUrl(sample.src)}
                className={`relative size-20 shrink-0 overflow-hidden rounded-2xl border-2 ${
                  photoUrl === sample.src ? 'border-remine-blue' : 'border-remine-border'
                }`}
              >
                <img src={sample.src} alt={sample.label} className="size-full object-cover" />
                {photoUrl === sample.src && (
                  <span className="absolute bottom-1 right-1 flex size-5 items-center justify-center rounded-full bg-remine-blue text-[11px] text-white">
                    ✓
                  </span>
                )}
              </button>
            ))}
          </div>
          <p className="text-[13px] text-remine-subtle">어머니 퀴즈에 자동으로 활용돼요!</p>
        </div>

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
            value={memoryLabel}
            onChange={(e) => setMemoryLabel(e.target.value)}
            placeholder="예) 2022년 여름 (50자 이내)"
            className="h-14 rounded-2xl border border-remine-border bg-remine-surfaceSoft px-4 text-[16px] placeholder:text-remine-muted focus:outline-none"
          />
        </div>

        {error && <p className="text-[13px] text-remine-pink">{error}</p>}

        <button
          type="button"
          onClick={handleSubmit}
          disabled={!title || !memoryLabel || !photoUrl || submitting}
          className="h-[52px] rounded-2xl bg-remine-blue text-[16px] font-semibold text-white disabled:opacity-40"
        >
          {submitting ? '전달 중...' : '어머니께 전달하기'}
        </button>
      </div>
    </BottomSheet>
  )
}
