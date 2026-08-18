import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import BottomSheet from '@/components/BottomSheet'
import { sendMessage } from '@/api/message'
import { COLORS } from '@/theme'
import sendIcon from '@/assets/icons/send.svg'

type MessageComposeSheetProps = {
  title: string
  quickReplies: string[]
  successTitle: string
  onSent: () => void
}

export default function MessageComposeSheet({ title, quickReplies, successTitle, onSent }: MessageComposeSheetProps) {
  const navigate = useNavigate()

  const [selectedIndex, setSelectedIndex] = useState<number | null>(null)
  const [customText, setCustomText] = useState('')
  const [sending, setSending] = useState(false)
  const [sent, setSent] = useState(false)
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
      setSent(true)
    } catch {
      setError('메시지를 보내지 못했어요. 잠시 후 다시 시도해 주세요.')
      setSending(false)
    }
  }

  if (sent) {
    return (
      <BottomSheet onDismiss={onSent}>
        <div className="flex flex-col items-center gap-1 pb-2 pt-6 text-center">
          <img src={sendIcon} alt="" className="mb-4 size-[50px]" />
          <h1 className="text-[20px] font-semibold text-remine-nearBlack">{successTitle}</h1>
          <p className="text-[14px] text-remine-subtle">어머니 앱에 알림이 전송되었어요.</p>
        </div>
        <button
          type="button"
          onClick={onSent}
          className="h-[56px] rounded-2xl bg-remine-nearBlack text-[18px] font-semibold text-white"
        >
          확인
        </button>
      </BottomSheet>
    )
  }

  return (
    <BottomSheet>
      <div className="flex items-center justify-between">
        <h1 className="text-[20px] font-semibold text-remine-dark">{title}</h1>
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
