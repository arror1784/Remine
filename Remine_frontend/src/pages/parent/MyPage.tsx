import { useNavigate } from 'react-router-dom'
import Screen from '@/components/Screen'
import { COLORS } from '@/theme'

const WEEK = [
  { day: '수', done: true },
  { day: '목', done: true },
  { day: '금', done: true },
  { day: '토', done: true },
  { day: '일', done: true },
  { day: '월', done: false },
  { day: '오늘', done: false },
]

const ACCOUNT_ITEMS = [
  { emoji: '👤', title: '프로필 편집', desc: '이름, 사진 변경' },
  { emoji: '🎯', title: '건강 목표 설정', desc: '걸음 수, 수면, 외출 목표', dot: true },
]

const APP_ITEMS = [
  { emoji: '🔔', title: '알림 설정', desc: '일일 리마인드, 가족 알림' },
  { emoji: '🔒', title: '개인정보 보호', desc: '데이터 관리, 잠금 설정' },
  { emoji: '👥', title: '가족 관리', desc: '가족 초대 및 권한 설정' },
]

const INFO_ITEMS = [
  { emoji: '📋', title: '이용약관' },
  { emoji: '📄', title: '개인정보 처리방침' },
  { emoji: 'ℹ️', title: '버전 정보', value: 'v1.0.0' },
]

export default function ParentMyPage() {
  const navigate = useNavigate()

  return (
    <Screen>
      <div className="flex items-center justify-between px-5 pt-[max(30px,env(safe-area-inset-top))]">
        <h1 className="text-[22px] font-semibold text-remine-dark">마이페이지</h1>
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="flex size-9 items-center justify-center rounded-full bg-remine-border text-remine-subtle"
        >
          ✕
        </button>
      </div>

      <div className="flex flex-col gap-6 px-5 pb-10 pt-5">
        <div className="relative overflow-hidden rounded-3xl bg-remine-surfaceDark p-6">
          <div aria-hidden className="absolute -right-5 -top-5 size-[100px] rounded-full bg-remine-pink opacity-10 blur-[15px]" />
          <div className="flex items-center gap-4">
            <div className="flex size-[68px] items-center justify-center rounded-full border-2 border-remine-pink bg-remine-highlight text-2xl">👩</div>
            <div className="flex flex-1 flex-col gap-1">
              <p className="text-[20px] font-semibold text-white">윤정아님</p>
              <p className="text-[14px] text-white/45">REMIND 28일째</p>
              <div className="flex gap-2 pt-1.5">
                <div className="rounded-xl bg-white/8 px-2.5 py-1.5 text-center">
                  <p className="text-[15px] font-semibold text-remine-pink">28일</p>
                  <p className="text-[11px] text-white/35">연속</p>
                </div>
                <div className="rounded-xl bg-white/8 px-2.5 py-1.5 text-center">
                  <p className="text-[15px] font-semibold text-remine-blue">14회</p>
                  <p className="text-[11px] text-white/35">퀴즈</p>
                </div>
                <div className="rounded-xl bg-white/8 px-2.5 py-1.5 text-center">
                  <p className="text-[15px] font-semibold text-remine-orange">6장</p>
                  <p className="text-[11px] text-white/35">공유</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="flex flex-col gap-3 rounded-[20px] bg-white p-[18px]">
          <div className="flex items-center justify-between">
            <span className="text-[16px] text-remine-dark">이번 주 달성률</span>
            <span className="text-[14px] font-medium text-remine-blue">5 / 7일</span>
          </div>
          <div className="flex justify-center gap-[5px]">
            {WEEK.map(({ day, done }) => (
              <div key={day} className="flex flex-1 flex-col items-center gap-2">
                <div
                  className="flex h-[26px] w-full items-center justify-center rounded-[7px] text-[10px] text-white"
                  style={{ backgroundColor: done ? COLORS.blue : COLORS.border }}
                >
                  {done && '✓'}
                </div>
                <span className={`text-[10px] ${done ? 'text-remine-dark' : 'text-remine-muted'}`}>{day}</span>
              </div>
            ))}
          </div>
        </div>

        <div className="flex flex-col overflow-hidden rounded-[20px] bg-white">
          <p className="px-5 pb-2 pt-4 text-[12px] font-semibold tracking-wide text-remine-muted">계정</p>
          {ACCOUNT_ITEMS.map((item, i) => (
            <div key={item.title} className={`flex items-center gap-3.5 px-5 py-3.5 ${i > 0 ? 'border-t border-remine-surfaceAlt' : ''}`}>
              <div className="flex size-11 items-center justify-center rounded-2xl bg-remine-surface text-[15px]">{item.emoji}</div>
              <div className="flex flex-1 flex-col">
                <span className="flex items-center gap-1.5 text-[16px] font-medium text-remine-dark">
                  {item.title}
                  {item.dot && <span className="size-1.5 rounded-full bg-remine-blue" />}
                </span>
                <span className="text-[13px] text-remine-muted">{item.desc}</span>
              </div>
              <span className="text-remine-offline">›</span>
            </div>
          ))}
        </div>

        <div className="flex flex-col overflow-hidden rounded-[20px] bg-white">
          <p className="px-5 pb-2 pt-4 text-[12px] font-semibold tracking-wide text-remine-muted">앱 설정</p>
          {APP_ITEMS.map((item, i) => (
            <div key={item.title} className={`flex items-center gap-3.5 px-5 py-3.5 ${i > 0 ? 'border-t border-remine-surfaceAlt' : ''}`}>
              <div className="flex size-11 items-center justify-center rounded-2xl bg-remine-surface text-[15px]">{item.emoji}</div>
              <div className="flex flex-1 flex-col">
                <span className="text-[16px] font-medium text-remine-dark">{item.title}</span>
                <span className="text-[13px] text-remine-muted">{item.desc}</span>
              </div>
              <span className="text-remine-offline">›</span>
            </div>
          ))}
        </div>

        <div className="flex flex-col overflow-hidden rounded-[20px] bg-white">
          <p className="px-5 pb-2 pt-4 text-[12px] font-semibold tracking-wide text-remine-muted">앱 정보</p>
          {INFO_ITEMS.map((item, i) => (
            <div key={item.title} className={`flex items-center gap-3.5 px-5 py-3.5 ${i > 0 ? 'border-t border-remine-surfaceAlt' : ''}`}>
              <div className="flex size-11 items-center justify-center rounded-2xl bg-remine-surface text-[15px]">{item.emoji}</div>
              <span className="flex-1 text-[16px] font-medium text-remine-dark">{item.title}</span>
              {item.value && <span className="text-[14px] text-remine-muted">{item.value}</span>}
            </div>
          ))}
        </div>

        <button type="button" className="h-[52px] rounded-2xl border border-remine-border bg-white text-[16px] font-semibold text-remine-danger">
          로그아웃
        </button>
      </div>
    </Screen>
  )
}
