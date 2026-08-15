import { useState } from 'react'
import { Link } from 'react-router-dom'
import Screen from '@/components/Screen'
import ModeBar from '@/components/ModeBar'
import BottomTabBar from '@/components/BottomTabBar'
import familyTrip from '@/assets/memories/family-trip.png'
import grandchild from '@/assets/memories/grandchild.png'

const MEMBERS = [
  { id: 'jiyoung', emoji: '👧', name: '딸 지영', sub: '방금 전' },
  { id: 'minho', emoji: '👦', name: '아들 민호', sub: '2시간 전' },
  { id: 'jungsik', emoji: '👨', name: '남편 정식', sub: '3일 전' },
]

const FEED = [
  {
    id: 1,
    author: '딸 지영',
    time: '3분 전',
    text: '엄마 요즘 어떻게 지내세요? 오늘 날씨 좋은데 산책 다녀오셨어요?',
    photo: null as string | null,
  },
  {
    id: 2,
    author: '아들 민호',
    time: '24시간 전',
    text: '어머니, 이번 주말에 놀러갈게요! 손주들도 데려갈게요.',
    photo: familyTrip,
    caption: '2022년 봄 가족 여행 사진 추가했어요',
  },
  {
    id: 3,
    author: '딸 지영',
    time: '어제',
    text: '아버지 앨범 정리하다가 찾았어요! 손 잡은 사진이에요 ㅎㅎ',
    photo: grandchild,
    caption: '첫 손주 돌잔치 사진 추가했어요',
  },
  {
    id: 4,
    author: '아들 민호',
    time: '그저께',
    text: '엄마 이제 딸이 걸으시나요? 엄마 목요 뭐가 대단하셨어요!',
    photo: null,
  },
]

export default function ParentFamily() {
  const [selected, setSelected] = useState(MEMBERS[0].id)
  const member = MEMBERS.find((m) => m.id === selected) ?? MEMBERS[0]

  return (
    <Screen footer={<BottomTabBar role="parent" accentColor="#ff42ad" />}>
      <ModeBar label="부모님 모드 — 윤정아님" color="#ff42ad" />

      <div className="flex items-center justify-between px-5 py-3.5">
        <h1 className="text-[22px] font-semibold text-[#1a1a1a]">가족</h1>
        <button type="button" className="h-9 rounded-full bg-remine-pink px-4 text-[14px] font-semibold text-white">
          ＋ 가족 초대
        </button>
      </div>

      <div className="flex flex-col gap-6 px-5 pb-10">
        <div className="flex gap-4">
          {MEMBERS.map((m) => (
            <button key={m.id} type="button" onClick={() => setSelected(m.id)} className="flex flex-col items-center gap-1.5">
              <div
                className="flex size-14 items-center justify-center rounded-full border-2 bg-[#fff7cc] text-xl"
                style={{ borderColor: selected === m.id ? '#ff42ad' : 'transparent' }}
              >
                {m.emoji}
              </div>
              <span className="text-[12px] text-[#1a1a1a]">{m.name}</span>
              <span className="text-[10px] text-[#9a9c91]">{m.sub}</span>
            </button>
          ))}
          <div className="flex flex-col items-center gap-1.5">
            <div className="flex size-14 items-center justify-center rounded-full border border-dashed border-[#ebebeb] text-lg text-[#9a9c91]">
              ＋
            </div>
            <span className="text-[12px] text-[#9a9c91]">가족 추가</span>
          </div>
        </div>

        <div className="flex items-center gap-3.5 rounded-3xl bg-[#1c1c1c] p-4">
          <div className="flex size-11 items-center justify-center rounded-full bg-[#fff7cc] text-lg">{member.emoji}</div>
          <div className="flex-1">
            <p className="text-[16px] font-semibold text-white">{member.name}</p>
            <p className="text-[13px] text-white/45">온라인 · {member.sub} 활동</p>
          </div>
          <Link to="/parent/family/message" className="rounded-xl bg-remine-pink px-4 py-2.5 text-[13px] font-semibold text-white">
            메시지 보내기
          </Link>
          <button type="button" className="rounded-xl border border-white/20 px-4 py-2.5 text-[13px] font-semibold text-white">
            전화하기
          </button>
        </div>

        <div className="flex items-center justify-around rounded-3xl bg-white py-4">
          <div className="flex flex-col items-center gap-0.5">
            <span className="text-[20px] font-semibold text-[#1a1a1a]">6장</span>
            <span className="text-[12px] text-[#9a9c91]">공유 사진</span>
          </div>
          <div className="flex flex-col items-center gap-0.5">
            <span className="text-[20px] font-semibold text-[#1a1a1a]">8회</span>
            <span className="text-[12px] text-[#9a9c91]">전화한 횟수</span>
          </div>
          <div className="flex flex-col items-center gap-0.5">
            <span className="text-[20px] font-semibold text-[#1a1a1a]">18개</span>
            <span className="text-[12px] text-[#9a9c91]">메시지</span>
          </div>
        </div>

        <div className="flex flex-col gap-4">
          <h2 className="text-[18px] font-semibold text-[#1a1a1a]">가족 소식</h2>
          {FEED.map((post) => (
            <div key={post.id} className="flex flex-col gap-2.5 rounded-3xl bg-white p-4">
              <div className="flex items-center gap-2">
                <span className="flex size-8 items-center justify-center rounded-full bg-[#fff7cc] text-[13px]">👤</span>
                <span className="text-[14px] font-medium text-[#1a1a1a]">{post.author}</span>
                <span className="text-[12px] text-[#9a9c91]">{post.time}</span>
              </div>
              <p className="text-[15px] leading-[1.5] text-[#1a1a1a]">{post.text}</p>
              {post.photo && (
                <div className="overflow-hidden rounded-2xl">
                  <img src={post.photo} alt={post.caption} className="h-[180px] w-full object-cover" />
                  {post.caption && <p className="pt-1.5 text-[13px] text-[#9a9c91]">{post.caption}</p>}
                </div>
              )}
              <div className="flex gap-3 pt-1 text-[13px] text-[#9a9c91]">
                <button type="button">♡ 좋아요</button>
                <button type="button">💬 답글</button>
              </div>
            </div>
          ))}
        </div>

        <input
          placeholder="가족에게 메시지 남기기..."
          className="h-14 rounded-2xl border border-[#ebebeb] bg-white px-5 text-[15px] placeholder:text-[#9a9c91] focus:outline-none"
        />
      </div>
    </Screen>
  )
}
