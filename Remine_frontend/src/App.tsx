import { Routes, Route } from 'react-router-dom'
import Splash from '@/pages/Splash'
import OnboardingFlow from '@/pages/onboarding/OnboardingFlow'
import ParentHome from '@/pages/parent/Home'
import Placeholder from '@/components/Placeholder'

function App() {
  return (
    <div className="relative mx-auto min-h-screen w-full max-w-[480px] bg-remine-bg">
      <Routes>
        <Route path="/" element={<Splash />} />
        <Route path="/onboarding" element={<OnboardingFlow />} />

        <Route path="/parent/home" element={<ParentHome />} />
        <Route path="/parent/today" element={<Placeholder role="parent" accentColor="#ff42ad" label="오늘" />} />
        <Route path="/parent/memories" element={<Placeholder role="parent" accentColor="#ff42ad" label="추억" />} />
        <Route path="/parent/family" element={<Placeholder role="parent" accentColor="#ff42ad" label="가족" />} />

        <Route path="/child/home" element={<Placeholder role="child" accentColor="#37ceff" label="홈" />} />
        <Route path="/child/today" element={<Placeholder role="child" accentColor="#37ceff" label="오늘" />} />
        <Route path="/child/memories" element={<Placeholder role="child" accentColor="#37ceff" label="추억" />} />
        <Route path="/child/family" element={<Placeholder role="child" accentColor="#37ceff" label="가족" />} />
      </Routes>
    </div>
  )
}

export default App
