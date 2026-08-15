import { useNavigate } from 'react-router-dom'
import SuggestionSheet from '@/components/SuggestionSheet'

export default function QuizReminder() {
  const navigate = useNavigate()

  return (
    <SuggestionSheet
      emoji="🧩"
      title="오늘의 추억 퀴즈가 준비되었어요!"
      description="2022년 가족 여행 사진으로 준비된 퀴즈예요. 5분이면 충분하니 소중한 기억을 떠올리는 좋은 시간 가져보아요~"
      primaryLabel="퀴즈 시작하기"
      primaryColor="#ffb84d"
      onPrimary={() => navigate('/parent/memories/quiz')}
    />
  )
}
