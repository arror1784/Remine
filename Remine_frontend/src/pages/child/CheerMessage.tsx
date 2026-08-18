import { useNavigate, useParams } from 'react-router-dom'
import MessageComposeSheet from '@/components/MessageComposeSheet'

const QUICK_REPLIES: Record<string, string[]> = {
  breakfast: ['엄마 아침은 드셨어요?', '밥 챙겨 드세요~ 건강이 최우선이에요!', '오늘 뭐 드실 거예요?'],
  walk: ['엄마 오늘 날씨도 좋은데 가볍게 산책해요!', '산책 한 바퀴 어때요?', '조금만 더 걸으면 목표 달성이에요, 화이팅~'],
  quiz: ['엄마 오늘 퀴즈 풀어보셨어요?', '추억 퀴즈 기다리고 계세요 재밌을 거예요~', '퀴즈 풀고 나면 제가 결과 볼게요!'],
}

export default function CheerMessage() {
  const navigate = useNavigate()
  const { itemType } = useParams<{ itemType: string }>()
  const quickReplies = QUICK_REPLIES[itemType ?? ''] ?? []

  return (
    <MessageComposeSheet
      title="응원 메시지 보내기"
      quickReplies={quickReplies}
      successTitle="응원을 보냈어요!"
      onSent={() => navigate('/child/today', { replace: true, state: { cheeredItemId: itemType } })}
    />
  )
}
