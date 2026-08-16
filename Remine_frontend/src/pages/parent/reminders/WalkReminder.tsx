import { useNavigate } from 'react-router-dom'
import SuggestionSheet from '@/components/SuggestionSheet'
import { COLORS } from '@/theme'

export default function WalkReminder() {
  const navigate = useNavigate()

  return (
    <SuggestionSheet
      emoji="🏃"
      title="산책하러 나가볼까요?"
      description="20분 가벼운 산책이 기분 전환에 도움이 돼요. 편한 신발을 신고 나가보세요!"
      primaryLabel="산책 시작하기"
      primaryColor={COLORS.dark}
      onPrimary={() => navigate('/parent/home')}
    />
  )
}
