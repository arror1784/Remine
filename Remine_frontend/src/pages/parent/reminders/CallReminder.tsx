import { useNavigate } from 'react-router-dom'
import SuggestionSheet from '@/components/SuggestionSheet'
import { COLORS } from '@/theme'

export default function CallReminder() {
  const navigate = useNavigate()

  return (
    <SuggestionSheet
      emoji="📞"
      title="가족에게 전화해볼까요?"
      description="짧은 통화 한 번이 사회적 활동 목표 달성에 도움이 돼요. 가족의 목소리 듣는 것만으로도 기분이 좋아져요!"
      primaryLabel="전화 연결하기"
      primaryColor={COLORS.pink}
      onPrimary={() => navigate('/parent/family/call')}
    />
  )
}
