import { Routes, Route } from 'react-router-dom'
import PhoneFrame from '@/components/PhoneFrame'
import Splash from '@/pages/Splash'
import OnboardingFlow from '@/pages/onboarding/OnboardingFlow'
import ParentHome from '@/pages/parent/Home'
import ParentToday from '@/pages/parent/Today'
import ParentMyPage from '@/pages/parent/MyPage'
import MemoryGallery from '@/pages/parent/memories/Gallery'
import MemoryQuiz from '@/pages/parent/memories/Quiz'
import ParentFamily from '@/pages/parent/Family'
import ParentMessage from '@/pages/parent/Message'
import Placeholder from '@/components/Placeholder'

function App() {
  return (
    <PhoneFrame>
      <Routes>
        <Route path="/" element={<Splash />} />
        <Route path="/onboarding" element={<OnboardingFlow />} />

        <Route path="/parent/home" element={<ParentHome />} />
        <Route path="/parent/today" element={<ParentToday />} />
        <Route path="/parent/mypage" element={<ParentMyPage />} />
        <Route path="/parent/memories" element={<MemoryGallery />} />
        <Route path="/parent/memories/quiz" element={<MemoryQuiz />} />
        <Route path="/parent/family" element={<ParentFamily />} />
        <Route path="/parent/family/message" element={<ParentMessage />} />

        <Route path="/child/home" element={<Placeholder role="child" accentColor="#37ceff" label="홈" />} />
        <Route path="/child/today" element={<Placeholder role="child" accentColor="#37ceff" label="오늘" />} />
        <Route path="/child/memories" element={<Placeholder role="child" accentColor="#37ceff" label="추억" />} />
        <Route path="/child/family" element={<Placeholder role="child" accentColor="#37ceff" label="가족" />} />
      </Routes>
    </PhoneFrame>
  )
}

export default App
