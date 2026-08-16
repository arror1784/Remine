import { useEffect } from 'react'
import { Routes, Route, useLocation } from 'react-router-dom'
import type { Location } from 'react-router-dom'
import { useAuthStore } from '@/store/auth'
import PhoneFrame from '@/components/PhoneFrame'
import Splash from '@/pages/Splash'
import Login from '@/pages/Login'
import OnboardingFlow from '@/pages/onboarding/OnboardingFlow'
import SwitchMode from '@/pages/SwitchMode'
import ParentHome from '@/pages/parent/Home'
import ParentToday from '@/pages/parent/Today'
import ParentMyPage from '@/pages/parent/MyPage'
import ParentNotifications from '@/pages/parent/Notifications'
import MemoryGallery from '@/pages/parent/memories/Gallery'
import MemoryQuiz from '@/pages/parent/memories/Quiz'
import ParentFamily from '@/pages/parent/Family'
import ParentMessage from '@/pages/parent/Message'
import ParentCall from '@/pages/parent/Call'
import WalkReminder from '@/pages/parent/reminders/WalkReminder'
import CallReminder from '@/pages/parent/reminders/CallReminder'
import QuizReminder from '@/pages/parent/reminders/QuizReminder'
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
  const location = useLocation()
  const backgroundLocation = (location.state as { backgroundLocation?: Location } | null)?.backgroundLocation
  const activeRole = useAuthStore((s) => s.activeRole)
  const setActiveRole = useAuthStore((s) => s.setActiveRole)

  // The visible screen's own /parent/* or /child/* path is the real source of
  // truth for which session should be active — not whichever switch button
  // was last tapped. This keeps every entry point (SwitchMode, /login, direct
  // URLs, back/forward) self-correcting instead of each one having to
  // remember to call setActiveRole itself.
  useEffect(() => {
    const pathname = (backgroundLocation ?? location).pathname
    const routeRole = pathname.startsWith('/child') ? 'child' : pathname.startsWith('/parent') ? 'parent' : null
    if (routeRole && routeRole !== activeRole) {
      setActiveRole(routeRole)
    }
  }, [location, backgroundLocation, activeRole, setActiveRole])

  return (
    <PhoneFrame>
      <div className={`h-full w-full transition-[filter] duration-200 ${backgroundLocation ? 'blur-sm pointer-events-none select-none' : ''}`}>
        <Routes location={backgroundLocation || location}>
          <Route path="/" element={<Splash />} />
          <Route path="/login" element={<Login />} />
          <Route path="/onboarding" element={<OnboardingFlow />} />

          <Route path="/parent/home" element={<ParentHome />} />
          <Route path="/parent/today" element={<ParentToday />} />
          <Route path="/parent/mypage" element={<ParentMyPage />} />
          <Route path="/parent/memories" element={<MemoryGallery />} />
          <Route path="/parent/memories/quiz" element={<MemoryQuiz />} />
          <Route path="/parent/family" element={<ParentFamily />} />
          <Route path="/parent/family/message" element={<ParentMessage />} />
          <Route path="/parent/family/call" element={<ParentCall />} />
          <Route path="/parent/reminders/walk" element={<WalkReminder />} />
          <Route path="/parent/reminders/call" element={<CallReminder />} />
          <Route path="/parent/reminders/quiz" element={<QuizReminder />} />

          <Route path="/child/home" element={<ChildHome />} />
          <Route path="/child/today" element={<ChildToday />} />
          <Route path="/child/mypage" element={<ChildMyPage />} />
          <Route path="/child/memories" element={<ChildMemoryGallery />} />
          <Route path="/child/memories/add" element={<AddMemoryPhoto />} />
          <Route path="/child/family" element={<ChildFamily />} />
          <Route path="/child/family/message" element={<ChildMessage />} />
          <Route path="/child/family/call" element={<ChildCall />} />
        </Routes>
      </div>

      {backgroundLocation && (
        <div className="absolute inset-0 z-50">
          <Routes location={location}>
            <Route path="/switch-mode" element={<SwitchMode />} />
            <Route path="/parent/reminders/walk" element={<WalkReminder />} />
            <Route path="/parent/reminders/call" element={<CallReminder />} />
            <Route path="/parent/reminders/quiz" element={<QuizReminder />} />
            <Route path="/parent/notifications" element={<ParentNotifications />} />
            <Route path="/child/memories/add" element={<AddMemoryPhoto />} />
            <Route path="/child/notifications" element={<ChildNotifications />} />
          </Routes>
        </div>
      )}
    </PhoneFrame>
  )
}

export default App
