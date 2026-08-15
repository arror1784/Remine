import { useState } from 'react'
import { Link } from 'react-router-dom'
import Screen from '@/components/Screen'
import ModeBar from '@/components/ModeBar'
import BottomTabBar from '@/components/BottomTabBar'
import familyTrip from '@/assets/memories/family-trip.png'
import grandchild from '@/assets/memories/grandchild.png'

const MEMBERS = [
  { id: 'jiyoung', emoji: '👧', name: '딸 지영', relation: '딸', color: '#37ceff', online: true, activity: '사진 4장 추가' },
  { id: 'minho', emoji: '👦', name: '아들 민호', relation: '아들', color: '#37ceff', online: false, activity: '사진 2장 추가' },
  { id: 'jungsik', emoji: '🧑', name: '남편 정식', relation: '남편', color: '#ff42ad', online: true, activity: '댓글 3개 추가' },
]

const FEED = [
  {
    id: 1,
    author: '딸 지영',
    emoji: '👧',
    color: '#ff42ad',
    time: '방금 전',
    text: '엄마 요즘 어떻게 지내세요? 오늘 날씨 좋던데 산책 다녀오셨어요?',
    photo: familyTrip,
    caption: '2022년 봄 가족 여행 사진 추가했어요 🌸',
  },
  {
    id: 2,
    author: '아들 민호',
    emoji: '👦',
    color: '#37ceff',
    time: '2시간 전',
    text: '어머니, 이번 주말에 놀러갈게요! 손주들도 같이 데려갈게요 😊',
    photo: null,
  },
  {
    id: 3,
    author: '딸 지영',
    emoji: '👧',
    color: '#ff42ad',
    time: '어제',
    text: '오래된 앨범 정리하다가 찾았어요! 손주 돌잔치 때 사진이에요 ㅎㅎ',
    photo: grandchild,
    caption: '첫 손주 돌잔치 사진 추가했어요 🎂',
  },
  {
    id: 4,
    author: '아들 민호',
    emoji: '👦',
    color: '#37ceff',
    time: '그저께',
    text: '엄마 어제 많이 걸으셨어요? 앱 보니까 목표 달성하셨던데 👍 대단해요!',
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
        <button type="button" className="h-9 rounded-full bg-[#fff7cc] px-4 text-[15px] font-semibold text-[#1a1a1a]">
          ＋ 가족 초대
        </button>
      </div>

      <div className="flex flex-col gap-6 px-5 pb-10">
        <div className="flex flex-col gap-3">
          <p className="text-[16px] text-[#1a1a1a]">함께하는 가족</p>
          <div className="flex gap-4 overflow-x-auto pb-1">
            {MEMBERS.map((m) => (
              <button key={m.id} type="button" onClick={() => setSelected(m.id)} className="flex w-20 shrink-0 flex-col items-center gap-1.5">
                <div className="relative">
                  <div
                    className="flex size-[60px] items-center justify-center rounded-full border-2 bg-[#f2f2ee] text-2xl"
                    style={{ borderColor: m.color }}
                  >
                    {m.emoji}
                  </div>
                  <span
                    className="absolute bottom-0.5 right-0.5 size-3 rounded-full border-2 border-remine-bg"
                    style={{ backgroundColor: m.online ? '#4caf50' : '#c9c9c9' }}
                  />
                </div>
                <span className="text-[14px] text-[#1a1a1a]">{m.name}</span>
                <span className="text-[12px] text-[#9a9c91]">{m.relation}</span>
                <span
                  className="whitespace-nowrap rounded-full px-2 py-0.5 text-[11px]"
                  style={{ backgroundColor: `${m.color}17`, color: m.color }}
                >
                  {m.activity}
                </span>
              </button>
            ))}
            <div className="flex w-20 shrink-0 flex-col items-center gap-1.5">
              <div className="flex size-[60px] items-center justify-center rounded-full border-2 border-dashed border-[#ddddd5] bg-[#f2f2ee] text-2xl text-[#9a9c91]">
                ＋
              </div>
              <span className="text-[14px] text-[#9a9c91]">가족 추가</span>
              <span className="text-[12px] text-[#ddddd5]">초대하기</span>
            </div>
          </div>
        </div>

        <div className="flex flex-col gap-4 rounded-[20px] bg-[#1c1c1c] px-5 pb-5 pt-6">
          <div className="flex items-center gap-3.5">
            <div
              className="flex size-[52px] items-center justify-center rounded-full border-2 bg-[#fff7cc] text-xl"
              style={{ borderColor: member.color }}
            >
              {member.emoji}
            </div>
            <div className="flex-1">
              <p className="text-[16px] font-semibold text-white">{member.name}</p>
              <p className="flex items-center gap-1.5 text-[13px] text-white/40">
                <span className="size-1.5 rounded-full" style={{ backgroundColor: member.online ? '#4caf50' : '#c9c9c9' }} />
                {member.online ? '온라인' : '오프라인'} · 방금 메세지
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
            <button type="button" className="flex h-11 flex-1 items-center justify-center rounded-xl border border-white/15 bg-white/10 text-[13.5px] font-semibold text-white">
              📞 전화하기
            </button>
          </div>
        </div>

        <div className="relative flex flex-col gap-3 overflow-hidden rounded-[20px] bg-[#1c1c1c] px-5 pb-5 pt-6">
          <div aria-hidden className="absolute -right-5 -top-4 size-20 rounded-full bg-remine-pink opacity-10 blur-[12px]" />
          <p className="text-[14px] text-white/40">가족들과 이번 달 함께한 활동</p>
          <div className="flex justify-around">
            <div className="flex flex-col items-center gap-1">
              <span className="text-[24px] font-semibold text-[#fff7cc]">6장</span>
              <span className="text-[13px] text-white/40">공유 사진</span>
            </div>
            <div className="flex flex-col items-center gap-1">
              <span className="text-[24px] font-semibold text-remine-blue">8회</span>
              <span className="text-[13px] text-white/40">함께한 퀴즈</span>
            </div>
            <div className="flex flex-col items-center gap-1">
              <span className="text-[24px] font-semibold text-remine-pink">18개</span>
              <span className="text-[13px] text-white/40">메시지</span>
            </div>
          </div>
        </div>

        <div className="flex flex-col">
          <h2 className="pb-3 text-[18px] font-semibold text-[#1a1a1a]">가족 소식</h2>
          {FEED.map((post, i) => (
            <div key={post.id} className={`flex flex-col gap-2.5 py-4 ${i < FEED.length - 1 ? 'border-b border-[#ebebeb]' : ''}`}>
              <div className="flex items-center gap-2.5">
                <div
                  className="flex size-[42px] items-center justify-center rounded-full border-2 bg-[#f2f2ee] text-[15px]"
                  style={{ borderColor: post.color }}
                >
                  {post.emoji}
                </div>
                <div>
                  <p className="text-[15px] text-[#1a1a1a]">{post.author}</p>
                  <p className="text-[13px] text-[#9a9c91]">{post.time}</p>
                </div>
              </div>
              <p className="text-[16px] leading-[1.6] text-[#1a1a1a]">{post.text}</p>
              {post.photo && (
                <div className="overflow-hidden rounded-2xl">
                  <img src={post.photo} alt={post.caption} className="h-[180px] w-full object-cover" />
                  <p className="bg-[#fff7cc] px-3.5 py-2.5 text-[13.5px] text-[#1a1a1a]">{post.caption}</p>
                </div>
              )}
              <div className="flex gap-2">
                <button type="button" className="flex h-9 items-center gap-1.5 rounded-full bg-[#f2f2ee] px-3.5 text-[14px] text-[#66695d]">
                  <span className="text-remine-pink">❤</span> 좋아요
                </button>
                <button type="button" className="flex h-9 items-center gap-1.5 rounded-full bg-[#f2f2ee] px-3.5 text-[14px] text-[#66695d]">
                  <span className="text-remine-blue">💬</span> 답글
                </button>
              </div>
            </div>
          ))}
        </div>

        <div className="flex flex-col gap-3">
          <p className="text-[16.9px] font-semibold text-[#1a1a1a]">가족에게 메시지 남기기</p>
          <textarea
            placeholder="오늘 있었던 일을 가족들과 나눠보세요..."
            className="h-[97px] resize-none rounded-2xl border border-[#ebebeb] bg-white px-4 py-3.5 text-[16px] placeholder:text-[#1a1a1a]/50 focus:outline-none"
          />
        </div>
      </div>
    </Screen>
  )
}
