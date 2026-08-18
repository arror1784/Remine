import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import MessageComposeSheet from '@/components/MessageComposeSheet'
import { getCheerMessageSuggestions } from '@/api/activity'

export default function CheerMessage() {
  const navigate = useNavigate()
  const { checklistItemId } = useParams<{ checklistItemId: string }>()

  const [suggestions, setSuggestions] = useState<string[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!checklistItemId) return
    let active = true
    getCheerMessageSuggestions(checklistItemId)
      .then((result) => {
        if (active) setSuggestions(result)
      })
      .catch(() => {
        // Falls back to an empty quick-reply list — the free-text box still works.
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [checklistItemId])

  return (
    <MessageComposeSheet
      title="응원 메시지 보내기"
      quickReplies={suggestions}
      quickRepliesLoading={loading}
      successTitle="응원을 보냈어요!"
      onSent={() => navigate('/child/today', { replace: true, state: { cheeredItemId: checklistItemId } })}
    />
  )
}
