import { Link } from 'react-router-dom'
import Screen from '@/components/Screen'
import ModeBar from '@/components/ModeBar'
import BottomTabBar from '@/components/BottomTabBar'

const SUMMARY = [
  { emoji: '🌙', label: '수면', value: '7시간 12분', percent: 90, barColor: '#37ceff' },
  { emoji: '🚶', label: '활동량', value: '4,280보', percent: 55, barColor: '#ffb84d' },
  { emoji: '🌿', label: '외출', value: '1회', percent: 40, barColor: '#ffb84d' },
  { emoji: '💬', label: '사회 활동', value: '연락 없음', percent: 10, barColor: '#ddddd5' },
]

const PATTERNS = [
  { emoji: '🌙', label: '수면', value: '7시간 12분', tag: '안정', tagBg: '#e8f8ff', tagColor: '#2ba8d4' },
  { emoji: '🚶', label: '활동량', value: '4,280보', tag: '조금 적음', tagBg: '#fff4e0', tagColor: '#c4901f' },
  { emoji: '🌿', label: '외출', value: '1회', tag: '조금 적음', tagBg: '#fff4e0', tagColor: '#c4901f' },
  { emoji: '💬', label: '사회 활동', value: '연락 없음', tag: '오늘은 휴식', tagBg: '#f2f2ee', tagColor: '#9a9c91' },
]

const SUGGESTIONS = [
  { emoji: '🏃', dot: '#1a1a1a', title: '20분 산책', desc: '오후 2~4시 사이 가볍게', to: '/parent/reminders/walk' },
  { emoji: '📞', dot: '#ff42ad', title: '가족에게 전화하기', desc: '짧은 통화도 큰 힘이 돼요', to: '/parent/reminders/call' },
  { emoji: '🧩', dot: '#ffb84d', title: '오늘의 추억 퀴즈', desc: '5분이면 충분해요', to: '/parent/reminders/quiz' },
]

export default function ParentToday() {
  return (
    <Screen footer={<BottomTabBar role="parent" accentColor="#ff42ad" />}>
      <ModeBar label="부모님 모드 — 윤정아님" color="#ff42ad" />

      <div className="flex items-center gap-2 px-5 py-3.5">
        <h1 className="text-[22px] font-semibold text-[#1a1a1a]">오늘의 분석</h1>
        <span className="flex items-center gap-1.5 rounded-full bg-[#f2f2ee] px-3 py-1.5 text-[13px] font-semibold text-[#1a1a1a]">
          <span className="size-1.5 rounded-full bg-remine-blue" />
          AI 분석 완료
        </span>
      </div>

      <div className="flex flex-col gap-7 px-5 pb-8">
        <div className="flex flex-col gap-1.5">
          <p className="text-[15px] text-[#9a9c91]">2026년 8월 11일 화요일</p>
          <h2 className="text-[23px] font-semibold text-[#1a1a1a]">전반적으로 안정된 하루예요 🙂</h2>
          <p className="pt-1 text-[16px] leading-[1.5] text-[#66695d]">수면과 활동이 평소와 비슷해요. 활동량이 조금 적으니 오후에 가볍게 움직여 보세요.</p>
        </div>

        <div className="flex flex-col gap-4 rounded-[20px] border border-[#ebebeb] bg-white p-5">
          {SUMMARY.map((s, i) => (
            <div key={s.label} className={`flex gap-3.5 ${i < SUMMARY.length - 1 ? 'border-b border-[#f4f4f0] pb-4' : ''}`}>
              <span className="w-[26px] shrink-0 text-center text-[15px]">{s.emoji}</span>
              <div className="flex flex-1 flex-col gap-2">
                <div className="flex items-center justify-between">
                  <span className="text-[16px] text-[#1a1a1a]">{s.label}</span>
                  <span className="text-[16px] text-[#1a1a1a]">{s.value}</span>
                </div>
                <div className="h-2 overflow-hidden rounded-full bg-[#f0f0ea]">
                  <div className="h-full rounded-full" style={{ width: `${s.percent}%`, backgroundColor: s.barColor }} />
                </div>
              </div>
            </div>
          ))}
        </div>

        <div className="flex flex-col">
          <h2 className="pb-1 text-[20px] font-semibold text-[#1a1a1a]">생활 패턴 분석</h2>
          {PATTERNS.map((p, i) => (
            <div key={p.label} className={`flex items-center gap-3.5 py-[18px] ${i < PATTERNS.length - 1 ? 'border-b border-[#ebebeb]' : ''}`}>
              <div className="flex size-12 shrink-0 items-center justify-center rounded-2xl bg-[#f2f2ee] text-[17px]">{p.emoji}</div>
              <div className="flex flex-1 flex-col gap-1">
                <div className="flex items-center gap-2">
                  <span className="text-[17px] text-[#1a1a1a]">{p.label}</span>
                  <span className="rounded-full px-2.5 py-0.5 text-[12px]" style={{ backgroundColor: p.tagBg, color: p.tagColor }}>
                    {p.tag}
                  </span>
                </div>
                <span className="text-[15px] text-[#9a9c91]">{p.value}</span>
              </div>
              <span className="text-[#c9c9c9]">›</span>
            </div>
          ))}
        </div>

        <div className="flex flex-col gap-1">
          <div className="flex items-center gap-2 pb-1">
            <span className="size-2 rounded-sm bg-remine-pink" />
            <h2 className="text-[20px] font-semibold text-[#1a1a1a]">오늘 해볼 수 있는 활동</h2>
          </div>
          {SUGGESTIONS.map((s, i) => (
            <div key={s.title} className={`flex items-center gap-4 py-[18px] ${i < SUGGESTIONS.length - 1 ? 'border-b border-[#ebebeb]' : ''}`}>
              <div className="flex size-[52px] shrink-0 items-center justify-center rounded-2xl bg-[#f2f2ee] text-[18px]">{s.emoji}</div>
              <div className="flex flex-1 flex-col gap-1">
                <div className="flex items-center gap-2">
                  <span className="size-1.5 rounded-full" style={{ backgroundColor: s.dot }} />
                  <span className="text-[17px] text-[#1a1a1a]">{s.title}</span>
                </div>
                <span className="text-[14px] text-[#9a9c91]">{s.desc}</span>
              </div>
              <Link
                to={s.to}
                className="flex h-10 shrink-0 items-center justify-center rounded-xl bg-[#fff7cc] px-4 text-[15px] font-semibold text-[#1a1a1a]"
              >
                시작
              </Link>
            </div>
          ))}
        </div>
      </div>
    </Screen>
  )
}
