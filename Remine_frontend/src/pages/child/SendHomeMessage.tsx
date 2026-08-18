import { useNavigate } from 'react-router-dom'
import MessageComposeSheet from '@/components/MessageComposeSheet'

const QUICK_REPLIES = [
  '엄마 잘 지내고 계세요? 보고 싶어요 💕',
  '오늘 날씨 좋던데 산책 다녀오셨어요?',
  '이번 주말에 놀러갈게요! 뭐 드시고 싶으세요?',
]

export default function SendHomeMessage() {
  const navigate = useNavigate()

  return (
    <MessageComposeSheet
      title="어머니께 메시지 보내기"
      quickReplies={QUICK_REPLIES}
      successTitle="메시지를 보냈어요!"
      onSent={() => navigate('/child/home', { replace: true })}
    />
  )
}
