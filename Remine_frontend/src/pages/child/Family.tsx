import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import Screen from '@/components/Screen'
import ModeBar from '@/components/ModeBar'
import BottomTabBar from '@/components/BottomTabBar'
import { getFamilySummary, getPairedProfile } from '@/api/family'
import type { FamilySummary, UserResponse } from '@/api/family'
import springOuting from '@/assets/memories/family-trip.png'
import birthdayCake from '@/assets/memories/birthday-cake.png'
import { COLORS } from '@/theme'

const RECENT_CHAT = [
  { from: 'them', text: '맞아요 엄마! 산책 다녀오셨어요?', time: '오전 10:15' },
  { from: 'me', text: '응, 동네 한 바퀴 돌고 왔어. 기분이 좋네~', time: '오전 10:16' },
]

const SHARED_PHOTOS = [
  { photo: springOuting, label: '가족 여행' },
  { photo: birthdayCake, label: '어머니 생신' },
]

export default function ChildFamily() {
  const location = useLocation()
  const [paired, setPaired] = useState<UserResponse | null>(null)
  const [summary, setSummary] = useState<FamilySummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let cancelled = false
    const load = async () => {
      try {
        const counterpart = await getPairedProfile()
        if (cancelled) return
        setPaired(counterpart)
        if (counterpart) {
          const stats = await getFamilySummary()
          if (!cancelled) setSummary(stats)
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

  return (
    <Screen footer={<BottomTabBar role="child" accentColor={COLORS.blue} />}>
      <ModeBar label="자녀 모드 — 지영님" color={COLORS.blue} dark />

      <div className="px-5 py-3.5">
        <h1 className="text-[22px] font-semibold text-remine-dark">가족</h1>
      </div>

      <div className="flex flex-col gap-5 px-5 pb-10">
        <div className="relative overflow-hidden rounded-3xl bg-remine-surfaceDark p-5">
          {loading ? (
            <p className="py-4 text-center text-[14px] text-white/45">불러오는 중...</p>
          ) : paired ? (
            <>
              <div className="flex items-center gap-3.5">
                <div className="flex size-[52px] items-center justify-center rounded-full border-2 border-remine-pink bg-remine-highlight text-xl">
                  👩
                </div>
                <div className="flex-1">
                  <p className="text-[18px] font-semibold text-white">{paired.name}님</p>
                  <p className="text-[13px] text-white/45">
                    {new Date(paired.createdAt).toLocaleDateString('ko-KR')} 가입
                  </p>
                </div>
              </div>
              <div className="flex gap-2.5 pt-4">
                <Link
                  to="/child/family/message"
                  className="flex h-11 flex-1 items-center justify-center rounded-xl bg-remine-blue text-[13.5px] font-semibold text-white"
                >
                  💬 메시지 보내기
                </Link>
                <Link
                  to="/child/family/call"
                  className="flex h-11 flex-1 items-center justify-center rounded-xl border border-white/15 bg-white/10 text-[13.5px] font-semibold text-white"
                >
                  📞 전화하기
                </Link>
              </div>
            </>
          ) : (
            <div className="flex flex-col items-center gap-2 py-4 text-center">
              <span className="text-2xl">👨‍👩‍👧</span>
              <p className="text-[16px] font-semibold text-white">
                {failed ? '불러오지 못했어요' : '아직 연결된 부모님이 없어요'}
              </p>
              {!failed && (
                <p className="text-[13px] leading-[1.6] text-white/45">
                  초대 코드를 입력하면 부모님과 연결할 수 있어요.
                </p>
              )}
            </div>
          )}
        </div>

        <div className="flex flex-col gap-3 rounded-[20px] border border-remine-border bg-white p-5">
          <div className="flex items-center justify-between">
            <span className="text-[16px] font-semibold text-remine-dark">최근 대화</span>
            <Link to="/child/family/message" className="text-[13px] text-remine-blue">
              전체 보기 ›
            </Link>
          </div>
          {RECENT_CHAT.map((m, i) => (
            <div key={i} className="flex items-start gap-2.5">
              <span className="flex size-7 shrink-0 items-center justify-center rounded-full bg-remine-highlight text-[13px]">
                {m.from === 'them' ? '👩' : '👧'}
              </span>
              <div>
                <p className="text-[14px] leading-[1.4] text-remine-dark">{m.text}</p>
                <p className="text-[12px] text-remine-muted">{m.time}</p>
              </div>
            </div>
          ))}
        </div>

        <div className="flex flex-col gap-3">
          <div className="flex items-baseline justify-between">
            <h2 className="text-[16px] font-semibold text-remine-dark">공유한 사진</h2>
            <span className="text-[13px] text-remine-muted">어머니 퀴즈에 활용돼요</span>
          </div>
          <div className="flex gap-3">
            {SHARED_PHOTOS.map((p) => (
              <div key={p.label} className="w-[110px] shrink-0 overflow-hidden rounded-2xl bg-remine-surface">
                <img src={p.photo} alt={p.label} className="h-[90px] w-full object-cover" />
                <p className="px-2.5 py-2 text-[12px] text-remine-dark">{p.label}</p>
              </div>
            ))}
            <Link
              to="/child/memories/add"
              state={{ backgroundLocation: location }}
              className="flex w-[110px] shrink-0 flex-col items-center justify-center gap-1 rounded-2xl border border-dashed border-remine-border text-remine-pink"
            >
              <span className="text-xl">＋</span>
              <span className="text-[12px] text-remine-muted">추가</span>
            </Link>
          </div>
        </div>

        {summary && (
          <div className="flex items-center justify-around rounded-3xl border border-remine-border bg-white py-4">
            <div className="flex flex-col items-center gap-0.5">
              <span className="text-[20px] font-semibold text-remine-pink">{summary.messageCount}개</span>
              <span className="text-[12px] text-remine-muted">메시지</span>
            </div>
            <div className="flex flex-col items-center gap-0.5">
              <span className="text-[20px] font-semibold text-remine-orange">{summary.sharedPhotoCount}장</span>
              <span className="text-[12px] text-remine-muted">공유 사진</span>
            </div>
            <div className="flex flex-col items-center gap-0.5">
              <span className="text-[20px] font-semibold text-remine-blue">{summary.callCount}회</span>
              <span className="text-[12px] text-remine-muted">통화</span>
            </div>
          </div>
        )}
      </div>
    </Screen>
  )
}
