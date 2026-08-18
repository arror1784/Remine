import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import BottomSheet from '@/components/BottomSheet'
import { generateQuizQuestions, uploadPhoto, uploadPhotoImage } from '@/api/memory'
import addAPhotoIcon from '@/assets/icons/add-a-photo.svg'

export default function AddMemoryPhoto() {
  const navigate = useNavigate()
  const [title, setTitle] = useState('')
  const [memoryLabel, setMemoryLabel] = useState('')
  const [photoUrl, setPhotoUrl] = useState('')
  const [previewUrl, setPreviewUrl] = useState<string | null>(null)
  const [imageUploading, setImageUploading] = useState(false)
  const [imageError, setImageError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  useEffect(() => {
    return () => {
      if (previewUrl) URL.revokeObjectURL(previewUrl)
    }
  }, [previewUrl])

  const handleFileSelected = async (file: File | undefined) => {
    if (!file) return
    setImageError(null)
    setPhotoUrl('')
    const objectUrl = URL.createObjectURL(file)
    setPreviewUrl((prev) => {
      if (prev) URL.revokeObjectURL(prev)
      return objectUrl
    })

    setImageUploading(true)
    try {
      setPhotoUrl(await uploadPhotoImage(file))
    } catch {
      setImageError('사진을 올리지 못했어요. 다시 선택해 주세요.')
    } finally {
      setImageUploading(false)
    }
  }

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
          className="flex size-8 items-center justify-center rounded-full bg-remine-surface text-[16px] text-remine-subtle"
        >
          ✕
        </button>
      </div>

      <div className="flex flex-col gap-5">
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          className="hidden"
          onChange={(e) => void handleFileSelected(e.target.files?.[0])}
        />
        <button
          type="button"
          onClick={() => fileInputRef.current?.click()}
          className="relative flex h-[140px] flex-col items-center justify-center gap-[15px] overflow-hidden rounded-2xl border-[1.5px] border-dashed border-remine-pink bg-remine-highlight px-4 py-3"
        >
          {previewUrl ? (
            <>
              <img src={previewUrl} alt="선택한 사진" className="absolute inset-0 size-full object-cover" />
              <div className="absolute inset-0 flex flex-col items-center justify-center gap-1 bg-black/40 text-white">
                <span className="text-[14px] font-semibold">{imageUploading ? '올리는 중...' : '다시 선택하기'}</span>
              </div>
            </>
          ) : (
            <>
              <img src={addAPhotoIcon} alt="" className="size-6" />
              <span className="text-[14px] font-semibold text-remine-dark">사진 선택하기</span>
              <span className="text-[12px] font-medium text-remine-muted">어머니 퀴즈에 자동으로 활용돼요!</span>
            </>
          )}
        </button>
        {imageError && <p className="-mt-3 text-[13px] text-remine-pink">{imageError}</p>}

        <div className="flex flex-col gap-2">
          <label className="text-[13px] font-semibold text-remine-subtle">어떤 추억인가요?</label>
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="예) 속초 여행, 첫 손주 돌잔치..."
            className="h-14 rounded-2xl bg-remine-surfaceSoft px-4 text-[14px] placeholder:text-remine-borderSoft focus:outline-none"
          />
        </div>

        <div className="flex flex-col gap-2">
          <label className="text-[13px] font-semibold text-remine-subtle">언제의 사진인가요?</label>
          <input
            value={memoryLabel}
            onChange={(e) => setMemoryLabel(e.target.value)}
            placeholder="예) 2022년 여름"
            className="h-[47px] rounded-2xl border border-remine-border bg-remine-surfaceSoft px-4 text-[15px] placeholder:text-remine-borderSoft focus:outline-none"
          />
        </div>

        {error && <p className="text-[13px] text-remine-pink">{error}</p>}

        <button
          type="button"
          onClick={handleSubmit}
          disabled={!title || !memoryLabel || !photoUrl || imageUploading || submitting}
          className="h-[56px] rounded-2xl bg-remine-blue text-[18px] font-semibold text-white disabled:opacity-40"
        >
          {submitting ? '전달 중...' : '어머니께 전달하기'}
        </button>
      </div>
    </BottomSheet>
  )
}
