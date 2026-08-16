import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import Screen from '@/components/Screen'
import ModeBar from '@/components/ModeBar'
import BottomTabBar from '@/components/BottomTabBar'
import { ROLE_COLOR } from '@/pages/onboarding/types'
import {
  createPost,
  createReply,
  getFamilySummary,
  getMyProfile,
  getPairedProfile,
  listPosts,
  listReplies,
  toggleLike,
} from '@/api/family'
import type { FamilySummary, Post, Reply, UserResponse } from '@/api/family'
import { COLORS } from '@/theme'

const ROLE_STYLE: Record<UserResponse['role'], { emoji: string; color: string }> = {
  PARENT: { emoji: '👩', color: ROLE_COLOR.parent },
  CHILD: { emoji: '👧', color: ROLE_COLOR.child },
}

function relativeTime(iso: string) {
  const minutes = Math.floor((Date.now() - new Date(iso).getTime()) / 60000)
  if (minutes < 1) return '방금 전'
  if (minutes < 60) return `${minutes}분 전`
  if (minutes < 60 * 24) return `${Math.floor(minutes / 60)}시간 전`
  return `${Math.floor(minutes / (60 * 24))}일 전`
}

export default function ParentFamily() {
  const [me, setMe] = useState<UserResponse | null>(null)
  const [paired, setPaired] = useState<UserResponse | null>(null)
  const [posts, setPosts] = useState<Post[]>([])
  const [summary, setSummary] = useState<FamilySummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)

  const [openReplies, setOpenReplies] = useState<Record<string, boolean>>({})
  const [replies, setReplies] = useState<Record<string, Reply[]>>({})
  const [replyDrafts, setReplyDrafts] = useState<Record<string, string>>({})

  const [draft, setDraft] = useState('')
  const textareaRef = useRef<HTMLTextAreaElement>(null)

  useEffect(() => {
    let cancelled = false
    const load = async () => {
      try {
        const [profile, counterpart] = await Promise.all([getMyProfile(), getPairedProfile()])
        if (cancelled) return
        setMe(profile)
        setPaired(counterpart)
        if (counterpart) {
          const [feed, stats] = await Promise.all([listPosts(), getFamilySummary()])
          if (cancelled) return
          setPosts(feed)
          setSummary(stats)
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

  const authorOf = (authorUserId: string) => {
    const user = authorUserId === me?.id ? me : authorUserId === paired?.id ? paired : null
    if (!user) return { name: '가족', ...ROLE_STYLE.CHILD }
    return { name: user.name, ...ROLE_STYLE[user.role] }
  }

  const onToggleLike = async (postId: string) => {
    try {
      const result = await toggleLike(postId)
      setPosts((prev) =>
        prev.map((p) =>
          p.id === postId ? { ...p, likedByViewer: result.liked, likeCount: result.likeCount } : p
        )
      )
    } catch {
      setFailed(true)
    }
  }

  const onToggleReplies = async (postId: string) => {
    const nextOpen = !openReplies[postId]
    setOpenReplies((prev) => ({ ...prev, [postId]: nextOpen }))
    if (!nextOpen || replies[postId]) return
    try {
      const loaded = await listReplies(postId)
      setReplies((prev) => ({ ...prev, [postId]: loaded }))
    } catch {
      setFailed(true)
    }
  }

  const onSubmitReply = async (postId: string) => {
    const body = (replyDrafts[postId] ?? '').trim()
    if (!body) return
    try {
      const reply = await createReply(postId, body)
      setReplies((prev) => ({ ...prev, [postId]: [...(prev[postId] ?? []), reply] }))
      setReplyDrafts((prev) => ({ ...prev, [postId]: '' }))
      setPosts((prev) => prev.map((p) => (p.id === postId ? { ...p, replyCount: p.replyCount + 1 } : p)))
    } catch {
      setFailed(true)
    }
  }

  const postMessage = async () => {
    const body = draft.trim()
    if (!body) return
    try {
      const created = await createPost(body)
      setPosts((prev) => [{ ...created, likedByViewer: false, replyCount: 0 }, ...prev])
      setDraft('')
    } catch {
      setFailed(true)
    }
  }

  const pairedStyle = paired ? ROLE_STYLE[paired.role] : null

  return (
    <Screen footer={<BottomTabBar role="parent" accentColor={COLORS.pink} />}>
      <ModeBar label={me ? `부모님 모드 — ${me.name}님` : '부모님 모드'} color={COLORS.pink} />

      <div className="flex items-center justify-between px-5 py-3.5">
        <h1 className="text-[22px] font-semibold text-remine-dark">가족</h1>
        <button type="button" className="h-9 rounded-full bg-remine-highlight px-4 text-[15px] font-semibold text-remine-dark">
          ＋ 가족 초대
        </button>
      </div>

      <div className="flex flex-col gap-6 px-5 pb-10">
        {loading && <p className="py-10 text-center text-[15px] text-remine-muted">불러오는 중...</p>}

        {!loading && failed && !me && (
          <p className="py-10 text-center text-[15px] text-remine-muted">불러오지 못했어요</p>
        )}

        {!loading && me && !paired && (
          <div className="flex flex-col items-center gap-2 rounded-[20px] border border-remine-border bg-white px-5 py-10 text-center">
            <span className="text-3xl">👨‍👩‍👧</span>
            <p className="text-[16px] font-semibold text-remine-dark">아직 연결된 가족이 없어요</p>
            <p className="text-[14px] leading-[1.6] text-remine-muted">
              자녀가 초대 코드를 입력하면 여기에서 가족 소식을 함께 나눌 수 있어요.
            </p>
          </div>
        )}

        {!loading && paired && pairedStyle && (
          <>
            <div className="flex flex-col gap-3">
              <p className="text-[16px] text-remine-dark">함께하는 가족</p>
              <div className="flex flex-col gap-4 rounded-[20px] bg-remine-surfaceDark px-5 pb-5 pt-6">
                <div className="flex items-center gap-3.5">
                  <div
                    className="flex size-[52px] items-center justify-center rounded-full border-2 bg-remine-highlight text-xl"
                    style={{ borderColor: pairedStyle.color }}
                  >
                    {pairedStyle.emoji}
                  </div>
                  <div className="flex-1">
                    <p className="text-[16px] font-semibold text-white">{paired.name}</p>
                    <p className="text-[13px] text-white/40">
                      {new Date(paired.createdAt).toLocaleDateString('ko-KR')} 가입
                    </p>
                  </div>
                </div>
                <div className="flex gap-2.5">
                  <Link
                    to="/parent/family/message"
                    className="flex h-11 flex-1 items-center justify-center rounded-xl bg-remine-pink text-[13.5px] font-semibold text-white"
                  >
                    💬 메시지 보내기
                  </Link>
                  <Link
                    to="/parent/family/call"
                    className="flex h-11 flex-1 items-center justify-center rounded-xl border border-white/15 bg-white/10 text-[13.5px] font-semibold text-white"
                  >
                    📞 전화하기
                  </Link>
                </div>
              </div>
            </div>

            {summary && (
              <div className="relative flex flex-col gap-3 overflow-hidden rounded-[20px] bg-remine-surfaceDark px-5 pb-5 pt-6">
                <div aria-hidden className="absolute -right-5 -top-4 size-20 rounded-full bg-remine-pink opacity-10 blur-[12px]" />
                <p className="text-[14px] text-white/40">가족들과 함께한 활동</p>
                <div className="flex justify-around">
                  <div className="flex flex-col items-center gap-1">
                    <span className="text-[24px] font-semibold text-remine-highlight">{summary.sharedPhotoCount}장</span>
                    <span className="text-[13px] text-white/40">공유 사진</span>
                  </div>
                  <div className="flex flex-col items-center gap-1">
                    <span className="text-[24px] font-semibold text-remine-blue">{summary.quizTogetherCount}회</span>
                    <span className="text-[13px] text-white/40">함께한 퀴즈</span>
                  </div>
                  <div className="flex flex-col items-center gap-1">
                    <span className="text-[24px] font-semibold text-remine-pink">{summary.messageCount}개</span>
                    <span className="text-[13px] text-white/40">메시지</span>
                  </div>
                </div>
              </div>
            )}

            <div className="flex flex-col">
              <h2 className="pb-3 text-[18px] font-semibold text-remine-dark">가족 소식</h2>
              {posts.length === 0 && <p className="py-4 text-[15px] text-remine-muted">아직 올라온 소식이 없어요.</p>}
              {posts.map((post, i) => {
                const author = authorOf(post.authorUserId)
                const postReplies = replies[post.id]
                return (
                  <div
                    key={post.id}
                    className={`flex flex-col gap-2.5 py-4 ${i < posts.length - 1 ? 'border-b border-remine-border' : ''}`}
                  >
                    <div className="flex items-center gap-2.5">
                      <div
                        className="flex size-[42px] items-center justify-center rounded-full border-2 bg-remine-surface text-[15px]"
                        style={{ borderColor: author.color }}
                      >
                        {author.emoji}
                      </div>
                      <div>
                        <p className="text-[15px] text-remine-dark">{author.name}</p>
                        <p className="text-[13px] text-remine-muted">{relativeTime(post.createdAt)}</p>
                      </div>
                    </div>
                    <p className="text-[16px] leading-[1.6] text-remine-dark">{post.body}</p>
                    {post.photoUrl && (
                      <div className="overflow-hidden rounded-2xl">
                        <img src={post.photoUrl} alt={post.photoCaption ?? ''} className="h-[180px] w-full object-cover" />
                        {post.photoCaption && (
                          <p className="bg-remine-highlight px-3.5 py-2.5 text-[13.5px] text-remine-dark">{post.photoCaption}</p>
                        )}
                      </div>
                    )}
                    <div className="flex gap-2">
                      <button
                        type="button"
                        onClick={() => onToggleLike(post.id)}
                        className="flex h-9 items-center gap-1.5 rounded-full px-3.5 text-[14px]"
                        style={{
                          backgroundColor: post.likedByViewer ? COLORS.highlightPink : COLORS.surface,
                          color: post.likedByViewer ? COLORS.pink : COLORS.subtle,
                        }}
                      >
                        <span style={{ color: COLORS.pink }}>{post.likedByViewer ? '❤' : '♡'}</span> 좋아요
                        {post.likeCount > 0 ? ` ${post.likeCount}` : ''}
                      </button>
                      <button
                        type="button"
                        onClick={() => onToggleReplies(post.id)}
                        className="flex h-9 items-center gap-1.5 rounded-full bg-remine-surface px-3.5 text-[14px] text-remine-subtle"
                      >
                        <span className="text-remine-blue">💬</span>
                        {post.replyCount > 0 ? `답글 ${post.replyCount}개` : '답글 달기'}
                      </button>
                    </div>
                    {openReplies[post.id] && (
                      <div className="flex flex-col gap-2.5 rounded-2xl bg-remine-surface px-3.5 py-3">
                        {!postReplies && <p className="text-[13px] text-remine-muted">불러오는 중...</p>}
                        {postReplies?.map((reply) => {
                          const replyAuthor = authorOf(reply.authorUserId)
                          return (
                            <div key={reply.id} className="flex items-start gap-2.5">
                              <span className="flex size-7 shrink-0 items-center justify-center rounded-full bg-white text-[13px]">
                                {replyAuthor.emoji}
                              </span>
                              <div>
                                <p className="text-[13px] text-remine-muted">
                                  {replyAuthor.name} · {relativeTime(reply.createdAt)}
                                </p>
                                <p className="text-[14.5px] leading-[1.5] text-remine-dark">{reply.body}</p>
                              </div>
                            </div>
                          )
                        })}
                        <div className="flex gap-2">
                          <input
                            value={replyDrafts[post.id] ?? ''}
                            onChange={(e) => setReplyDrafts((prev) => ({ ...prev, [post.id]: e.target.value }))}
                            placeholder="답글을 남겨보세요"
                            className="h-10 flex-1 rounded-xl border border-remine-border bg-white px-3 text-[14.5px] placeholder:text-remine-muted focus:outline-none"
                          />
                          <button
                            type="button"
                            onClick={() => onSubmitReply(post.id)}
                            disabled={!(replyDrafts[post.id] ?? '').trim()}
                            className="h-10 rounded-xl bg-remine-pink px-4 text-[14px] font-semibold text-white disabled:opacity-40"
                          >
                            등록
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                )
              })}
            </div>

            <div className="flex flex-col gap-3">
              <p className="text-[16.9px] font-semibold text-remine-dark">가족에게 메시지 남기기</p>
              <textarea
                ref={textareaRef}
                value={draft}
                onChange={(e) => setDraft(e.target.value)}
                placeholder="오늘 있었던 일을 가족들과 나눠보세요..."
                className="h-[97px] resize-none rounded-2xl border border-remine-border bg-white px-4 py-3.5 text-[16px] placeholder:text-remine-dark/50 focus:outline-none"
              />
              <button
                type="button"
                onClick={postMessage}
                disabled={!draft.trim()}
                className="h-12 rounded-xl bg-remine-pink text-[15px] font-semibold text-white disabled:opacity-40"
              >
                남기기
              </button>
            </div>
          </>
        )}

        {!loading && failed && me && <p className="text-center text-[14px] text-remine-muted">불러오지 못했어요</p>}
      </div>
    </Screen>
  )
}
