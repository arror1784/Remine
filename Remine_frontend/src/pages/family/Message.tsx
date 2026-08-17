import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Screen from '@/components/Screen'
import { getPairedProfile } from '@/api/family'
import type { UserResponse } from '@/api/family'
import { getQuickReplies, getThread, sendMessage } from '@/api/message'
import type { ChatMessage, QuickReply } from '@/api/message'
import { useAuthStore } from '@/store/auth'
import { ROLE_COLOR } from '@/pages/onboarding/types'

const ROLE_STYLE: Record<UserResponse['role'], { emoji: string; color: string }> = {
  PARENT: { emoji: '👩', color: ROLE_COLOR.parent },
  CHILD: { emoji: '👧', color: ROLE_COLOR.child },
}

const EMPTY_STATE_COPY = {
  parent: '자녀가 초대 코드를 입력하면 여기에서 메시지를 주고받을 수 있어요.',
  child: '부모님의 초대 코드를 입력하면 여기에서 메시지를 주고받을 수 있어요.',
}

function clockTime(iso: string) {
  return new Date(iso).toLocaleTimeString('ko-KR', { hour: 'numeric', minute: '2-digit' })
}

// Shared by /parent/family/message and /child/family/message. Which role
// this instance is comes from the prop the route passes in, never from the
// activeRole auth store — that field is shared browser-wide (persisted to
// localStorage), so reading it here would break the two-incognito-tabs case
// (one tab open as parent, one as child) the moment either tab reloads.
type FamilyMessageProps = {
  role: 'parent' | 'child'
  accentColor: string
}

export default function FamilyMessage({ role, accentColor }: FamilyMessageProps) {
  const navigate = useNavigate()
  const [paired, setPaired] = useState<UserResponse | null>(null)
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [quickReplies, setQuickReplies] = useState<QuickReply[]>([])
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)
  const [draft, setDraft] = useState('')

  const myUserId = useAuthStore((s) => s.sessions[s.activeRole]?.userId)

  useEffect(() => {
    let cancelled = false
    const load = async () => {
      try {
        const counterpart = await getPairedProfile()
        if (cancelled) return
        setPaired(counterpart)
        if (counterpart) {
          const [thread, replies] = await Promise.all([getThread(), getQuickReplies()])
          if (cancelled) return
          setMessages(thread)
          setQuickReplies(replies)
        }
      } catch {
        if (!cancelled) setFailed(true)
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    load()
    return () => {
      cancelled = true
    }
  }, [])

  // No websocket/push infra exists yet, so poll for the counterpart's new
  // messages instead of requiring a manual refresh.
  useEffect(() => {
    if (!paired) return
    const interval = setInterval(() => {
      getThread()
        .then((thread) => {
          setMessages(thread)
          setFailed(false)
        })
        .catch(() => {})
    }, 4000)
    return () => clearInterval(interval)
  }, [paired])

  const send = async (text: string, quickReplyKey?: string) => {
    if (!text.trim()) return
    try {
      const sent = await sendMessage(text.trim(), quickReplyKey)
      setMessages((prev) => [...prev, sent])
      setDraft('')
      setFailed(false)
    } catch {
      setFailed(true)
    }
  }

  const pairedStyle = paired ? ROLE_STYLE[paired.role] : null

  return (
    <Screen
      footer={
        paired ? (
          <div className="flex flex-col gap-2.5 border-t border-remine-border bg-white px-5 pb-[max(20px,env(safe-area-inset-bottom))] pt-3">
            <div className="no-scrollbar flex gap-2 overflow-x-auto">
              {quickReplies.map((reply) => (
                <button
                  key={reply.id}
                  type="button"
                  onClick={() => send(reply.label, reply.id)}
                  className="shrink-0 whitespace-nowrap rounded-full border border-remine-border bg-remine-bg px-3.5 py-2 text-[13px] text-remine-dark"
                >
                  {reply.label}
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
                placeholder="메시지 입력..."
                className="h-12 min-w-0 flex-1 rounded-full bg-remine-surface px-4 text-[15px] focus:outline-none"
              />
              <button
                type="submit"
                className="flex size-12 shrink-0 items-center justify-center rounded-full text-white"
                style={{ backgroundColor: accentColor }}
              >
                ➤
              </button>
            </form>
          </div>
        ) : undefined
      }
    >
      <div className="sticky top-0 z-10 flex items-center gap-3 border-b border-remine-border bg-white px-4 pb-3 pt-[max(30px,env(safe-area-inset-top))]">
        <button type="button" onClick={() => navigate(-1)} className="text-[20px] text-remine-dark">
          ‹
        </button>
        {pairedStyle && (
          <span
            className="flex size-9 items-center justify-center rounded-full border-2 bg-remine-highlight text-[15px]"
            style={{ borderColor: pairedStyle.color }}
          >
            {pairedStyle.emoji}
          </span>
        )}
        <div className="flex-1">
          <p className="text-[16px] font-semibold text-remine-dark">{paired ? paired.name : '메시지'}</p>
        </div>
        {paired && (
          <Link to={`/${role}/family/call`} className="text-xl">
            📞
          </Link>
        )}
      </div>

      <div className="flex flex-col gap-3 px-4 py-4">
        {loading && <p className="py-10 text-center text-[15px] text-remine-muted">불러오는 중...</p>}

        {!loading && failed && !paired && (
          <p className="py-10 text-center text-[15px] text-remine-muted">불러오지 못했어요</p>
        )}

        {!loading && !failed && !paired && (
          <div className="flex flex-col items-center gap-2 rounded-[20px] border border-remine-border bg-white px-5 py-10 text-center">
            <span className="text-3xl">👨‍👩‍👧</span>
            <p className="text-[16px] font-semibold text-remine-dark">아직 연결된 가족이 없어요</p>
            <p className="text-[14px] leading-[1.6] text-remine-muted">{EMPTY_STATE_COPY[role]}</p>
          </div>
        )}

        {!loading && paired && messages.length === 0 && (
          <p className="py-10 text-center text-[15px] text-remine-muted">첫 메시지를 보내보세요.</p>
        )}

        {!loading &&
          paired &&
          messages.map((m) => {
            const mine = m.senderId === myUserId
            return (
              <div
                key={m.id}
                data-testid="message-bubble"
                data-mine={mine}
                className={`flex items-end gap-2 ${mine ? 'flex-row-reverse' : ''}`}
              >
                {!mine && pairedStyle && (
                  <span className="flex size-7 shrink-0 items-center justify-center rounded-full bg-remine-highlight text-[12px]">
                    {pairedStyle.emoji}
                  </span>
                )}
                <div
                  className={`max-w-[75%] rounded-2xl px-4 py-2.5 text-[15px] leading-[1.4] ${
                    mine ? 'text-white' : 'bg-remine-surface text-remine-dark'
                  }`}
                  style={mine ? { backgroundColor: accentColor } : undefined}
                >
                  {m.body}
                </div>
                <span className="shrink-0 text-[11px] text-remine-muted">{clockTime(m.createdAt)}</span>
              </div>
            )
          })}

        {!loading && failed && paired && <p className="text-center text-[14px] text-remine-muted">불러오지 못했어요</p>}
      </div>
    </Screen>
  )
}
