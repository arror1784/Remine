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
import ParentCall from '@/pages/parent/Call'
import ChildHome from '@/pages/child/Home'
import ChildToday from '@/pages/child/Today'
import ChildMyPage from '@/pages/child/MyPage'
import ChildMemoryGallery from '@/pages/child/memories/Gallery'
import AddMemoryPhoto from '@/pages/child/memories/AddPhoto'
import ChildFamily from '@/pages/child/Family'
import ChildMessage from '@/pages/child/Message'
import ChildCall from '@/pages/child/Call'
import ChildNotifications from '@/pages/child/Notifications'

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
        <Route path="/parent/family/call" element={<ParentCall />} />

        <Route path="/child/home" element={<ChildHome />} />
        <Route path="/child/today" element={<ChildToday />} />
        <Route path="/child/mypage" element={<ChildMyPage />} />
        <Route path="/child/notifications" element={<ChildNotifications />} />
        <Route path="/child/memories" element={<ChildMemoryGallery />} />
        <Route path="/child/memories/add" element={<AddMemoryPhoto />} />
        <Route path="/child/family" element={<ChildFamily />} />
        <Route path="/child/family/message" element={<ChildMessage />} />
        <Route path="/child/family/call" element={<ChildCall />} />
      </Routes>
    </PhoneFrame>
  )
}

export default App
