import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Screen from '@/components/Screen'

const INITIAL_MESSAGES = [
  { from: 'them', time: '오전 10:12', text: '지영아, 오늘 날씨 참 좋더라!' },
  { from: 'me', time: '오전 10:15', text: '맞아요 엄마! 아침 산책 다녀오셨어요?' },
  { from: 'them', time: '오전 10:16', text: '응 아빠랑 동네 한 바퀴 돌고 왔어. 기분 좋네~' },
  { from: 'me', time: '오전 10:18', text: '다행이에요 😊 아침 밥은 챙겨 드셨어요?' },
  { from: 'them', time: '오전 10:19', text: '웅~ 아침에는 그릭요거트에 블루베리 먹었지' },
]

const QUICK_REPLIES = ['잘 지내고 계세요? 💕', '오늘도 화이팅이에요!', '보고 싶어요 엄마 😊']

export default function ChildMessage() {
  const navigate = useNavigate()
  const [messages, setMessages] = useState(INITIAL_MESSAGES)
  const [draft, setDraft] = useState('')

  const send = (text: string) => {
    if (!text.trim()) return
    setMessages((prev) => [...prev, { from: 'me', time: '방금', text }])
    setDraft('')
  }

  return (
    <Screen
      footer={
        <div className="flex flex-col gap-2.5 border-t border-[#ebebeb] bg-white px-5 pb-[max(20px,env(safe-area-inset-bottom))] pt-3">
          <div className="no-scrollbar flex gap-2 overflow-x-auto">
            {QUICK_REPLIES.map((reply) => (
              <button
                key={reply}
                type="button"
                onClick={() => send(reply)}
                className="shrink-0 whitespace-nowrap rounded-full border border-[#ebebeb] bg-remine-bg px-3.5 py-2 text-[13px] text-[#1a1a1a]"
              >
                {reply}
              </button>
            ))}
          </div>
          <form
            onSubmit={(e) => {
              e.preventDefault()
              send(draft)
            }}
            className="flex items-center gap-3"
          >
            <input
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
              placeholder="어머니께 메시지..."
              className="h-12 min-w-0 flex-1 rounded-full bg-[#f2f2ee] px-4 text-[15px] focus:outline-none"
            />
            <button type="submit" className="flex size-12 shrink-0 items-center justify-center rounded-full bg-remine-blue text-white">
              ➤
            </button>
          </form>
        </div>
      }
    >
      <div className="flex items-center gap-3 border-b border-[#ebebeb] bg-white px-4 pb-3 pt-[max(30px,env(safe-area-inset-top))]">
        <button type="button" onClick={() => navigate(-1)} className="text-[20px] text-[#1a1a1a]">
          ‹
        </button>
        <span className="flex size-9 items-center justify-center rounded-full bg-[#fff7cc] text-[15px]">👩</span>
        <div className="flex-1">
          <p className="text-[16px] font-semibold text-[#1a1a1a]">윤정아님 (어머니)</p>
          <p className="flex items-center gap-1 text-[12px] text-remine-blue">
            <span className="size-1.5 rounded-full bg-remine-blue" /> 온라인
          </p>
        </div>
        <Link to="/child/family/call" className="text-xl">
          📞
        </Link>
      </div>

      <div className="flex flex-col gap-3 px-4 py-4">
        {messages.map((m, i) => (
          <div key={i} className={`flex items-end gap-2 ${m.from === 'me' ? 'flex-row-reverse' : ''}`}>
            {m.from === 'them' && (
              <span className="flex size-7 shrink-0 items-center justify-center rounded-full bg-[#fff7cc] text-[12px]">👩</span>
            )}
            <div
              className={`max-w-[75%] rounded-2xl px-4 py-2.5 text-[15px] leading-[1.4] ${
                m.from === 'me' ? 'bg-remine-blue text-white' : 'bg-[#f2f2ee] text-[#1a1a1a]'
              }`}
            >
              {m.text}
            </div>
            <span className="shrink-0 text-[11px] text-[#9a9c91]">{m.time}</span>
          </div>
        ))}
      </div>
    </Screen>
  )
}
