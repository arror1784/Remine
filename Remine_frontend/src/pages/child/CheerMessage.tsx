import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import BottomSheet from '@/components/BottomSheet'
import { sendMessage } from '@/api/message'
import { COLORS } from '@/theme'

const QUICK_REPLIES: Record<string, string[]> = {
  breakfast: ['엄마 아침은 드셨어요?', '밥 챙겨 드세요~ 건강이 최우선이에요!', '오늘 뭐 드실 거예요?'],
  walk: ['엄마 오늘 날씨도 좋은데 가볍게 산책해요!', '산책 한 바퀴 어때요?', '조금만 더 걸으면 목표 달성이에요, 화이팅~'],
  quiz: ['엄마 오늘 퀴즈 풀어보셨어요?', '추억 퀴즈 기다리고 계세요 재밌을 거예요~', '퀴즈 풀고 나면 제가 결과 볼게요!'],
}

export default function CheerMessage() {
  const navigate = useNavigate()
  const { itemType } = useParams<{ itemType: string }>()
  const quickReplies = QUICK_REPLIES[itemType ?? ''] ?? []

  const [selectedIndex, setSelectedIndex] = useState<number | null>(null)
  const [customText, setCustomText] = useState('')
  const [sending, setSending] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const finalText = customText.trim() || (selectedIndex !== null ? quickReplies[selectedIndex] : '')
  const customSelected = customText.trim().length > 0

  const selectQuickReply = (index: number) => {
    setSelectedIndex(index)
    setCustomText('')
  }

  const handleCustomTextChange = (value: string) => {
    setCustomText(value)
    if (value.trim()) setSelectedIndex(null)
  }

  const handleSend = async () => {
    if (!finalText) return
    setSending(true)
    setError(null)
    try {
      await sendMessage(finalText)
      navigate('/child/today', { replace: true, state: { cheeredItemId: itemType } })
    } catch {
      setError('메시지를 보내지 못했어요. 잠시 후 다시 시도해 주세요.')
      setSending(false)
    }
  }

  return (
    <BottomSheet>
      <div className="flex items-center justify-between">
        <h1 className="text-[20px] font-semibold text-remine-dark">응원 메시지 보내기</h1>
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="flex size-8 items-center justify-center rounded-full bg-remine-surface text-[16px] text-remine-subtle"
        >
          ✕
        </button>
      </div>

      <div className="flex flex-col gap-2">
        {quickReplies.map((reply, index) => {
          const selected = selectedIndex === index
          return (
            <button
              key={reply}
              type="button"
              onClick={() => selectQuickReply(index)}
              className="flex w-full flex-col items-start rounded-2xl border px-4 py-3 text-left"
              style={{
                backgroundColor: selected ? COLORS.highlight : COLORS.surfaceSoft,
                borderColor: selected ? COLORS.pink : 'transparent',
              }}
            >
              <span className="text-[14px] text-remine-dark">{reply}</span>
            </button>
          )
        })}

        <textarea
          value={customText}
          onChange={(e) => handleCustomTextChange(e.target.value)}
          placeholder="직접 입력하기..."
          rows={3}
          className="h-[98px] resize-none rounded-2xl border px-4 py-3 text-[15px] placeholder:text-remine-borderSoft focus:outline-none"
          style={{
            backgroundColor: customSelected ? COLORS.highlight : COLORS.surfaceSoft,
            borderColor: customSelected ? COLORS.pink : COLORS.border,
          }}
        />
      </div>

      {error && <p className="text-[13px] text-remine-pink">{error}</p>}

      <button
        type="button"
        onClick={handleSend}
        disabled={!finalText || sending}
        className="h-[56px] rounded-2xl text-[18px] font-semibold text-white"
        style={{ backgroundColor: finalText ? COLORS.blue : COLORS.muted }}
      >
        {sending ? '보내는 중...' : '보내기'}
      </button>
    </BottomSheet>
  )
}
