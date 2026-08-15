import { Routes, Route } from 'react-router-dom'
import Onboarding from '@/pages/Onboarding'

function App() {
  return (
    <div className="relative w-[402px] max-w-full min-h-[874px] overflow-hidden bg-remine-bg shadow-2xl sm:rounded-[48px]">
      <Routes>
        <Route path="/" element={<Onboarding />} />
      </Routes>
    </div>
  )
}

export default App
