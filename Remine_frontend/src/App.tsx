import { useEffect } from 'react'
import { Routes, Route, useLocation } from 'react-router-dom'
import type { Location } from 'react-router-dom'
import { useAuthStore } from '@/store/auth'
import { COLORS } from '@/theme'
import PhoneFrame from '@/components/PhoneFrame'
import IncomingCallBanner from '@/components/IncomingCallBanner'
import Splash from '@/pages/Splash'
import Login from '@/pages/Login'
import OnboardingFlow from '@/pages/onboarding/OnboardingFlow'
import SwitchMode from '@/pages/SwitchMode'
import ParentHome from '@/pages/parent/Home'
import ParentToday from '@/pages/parent/Today'
import ParentMyPage from '@/pages/parent/MyPage'
import MemoryGallery from '@/pages/parent/memories/Gallery'
import MemoryQuiz from '@/pages/parent/memories/Quiz'
import ParentFamily from '@/pages/parent/Family'
import WalkReminder from '@/pages/parent/reminders/WalkReminder'
import CallReminder from '@/pages/parent/reminders/CallReminder'
import QuizReminder from '@/pages/parent/reminders/QuizReminder'
import ChildHome from '@/pages/child/Home'
import ChildToday from '@/pages/child/Today'
import ChildMyPage from '@/pages/child/MyPage'
import ChildMemoryGallery from '@/pages/child/memories/Gallery'
import AddMemoryPhoto from '@/pages/child/memories/AddPhoto'
import AnswerQuiz from '@/pages/child/memories/AnswerQuiz'
import ChildFamily from '@/pages/child/Family'
import FamilyMessage from '@/pages/family/Message'
import FamilyCall from '@/pages/family/Call'
import FamilyNotifications from '@/pages/family/Notifications'

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

          <Route path="/switch-mode" element={<SwitchMode />} />

          <Route path="/parent/home" element={<ParentHome />} />
          <Route path="/parent/today" element={<ParentToday />} />
          <Route path="/parent/mypage" element={<ParentMyPage />} />
          <Route path="/parent/notifications" element={<FamilyNotifications role="parent" accentColor={COLORS.pink} />} />
          <Route path="/parent/memories" element={<MemoryGallery />} />
          <Route path="/parent/memories/quiz" element={<MemoryQuiz />} />
          <Route path="/parent/memories/:photoId/quiz" element={<MemoryQuiz />} />
          <Route path="/parent/family" element={<ParentFamily />} />
          <Route path="/parent/family/message" element={<FamilyMessage role="parent" accentColor={COLORS.pink} />} />
          <Route path="/parent/family/call" element={<FamilyCall role="parent" />} />
          <Route path="/parent/reminders/walk" element={<WalkReminder />} />
          <Route path="/parent/reminders/call" element={<CallReminder />} />
          <Route path="/parent/reminders/quiz" element={<QuizReminder />} />

          <Route path="/child/home" element={<ChildHome />} />
          <Route path="/child/today" element={<ChildToday />} />
          <Route path="/child/mypage" element={<ChildMyPage />} />
          <Route path="/child/notifications" element={<FamilyNotifications role="child" accentColor={COLORS.blue} />} />
          <Route path="/child/memories" element={<ChildMemoryGallery />} />
          <Route path="/child/memories/add" element={<AddMemoryPhoto />} />
          <Route path="/child/memories/:photoId/answer-quiz" element={<AnswerQuiz />} />
          <Route path="/child/family" element={<ChildFamily />} />
          <Route path="/child/family/message" element={<FamilyMessage role="child" accentColor={COLORS.blue} />} />
          <Route path="/child/family/call" element={<FamilyCall role="child" />} />
        </Routes>
      </div>

      {backgroundLocation && (
        <div className="absolute inset-0 z-50">
          <Routes location={location}>
            <Route path="/switch-mode" element={<SwitchMode />} />
            <Route path="/parent/reminders/walk" element={<WalkReminder />} />
            <Route path="/parent/reminders/call" element={<CallReminder />} />
            <Route path="/parent/reminders/quiz" element={<QuizReminder />} />
            <Route path="/parent/notifications" element={<FamilyNotifications role="parent" accentColor={COLORS.pink} />} />
            <Route path="/child/memories/add" element={<AddMemoryPhoto />} />
            <Route path="/child/notifications" element={<FamilyNotifications role="child" accentColor={COLORS.blue} />} />
          </Routes>
        </div>
      )}

      <IncomingCallBanner />
    </PhoneFrame>
  )
}

export default App
