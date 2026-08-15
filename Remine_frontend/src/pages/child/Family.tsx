import { Link } from 'react-router-dom'
import Screen from '@/components/Screen'
import ModeBar from '@/components/ModeBar'
import BottomTabBar from '@/components/BottomTabBar'
import springOuting from '@/assets/memories/family-trip.png'
import birthdayCake from '@/assets/memories/birthday-cake.png'

const RECENT_CHAT = [
  { from: 'them', text: '맞아요 엄마! 산책 다녀오셨어요?', time: '오전 10:15' },
  { from: 'me', text: '응, 동네 한 바퀴 돌고 왔어. 기분이 좋네~', time: '오전 10:16' },
]

const SHARED_PHOTOS = [
  { photo: springOuting, label: '가족 여행' },
  { photo: birthdayCake, label: '어머니 생신' },
]

export default function ChildFamily() {
  return (
    <Screen footer={<BottomTabBar role="child" accentColor="#37ceff" />}>
      <ModeBar label="자녀 모드 — 지영님" color="#37ceff" dark />

      <div className="px-5 py-3.5">
        <h1 className="text-[22px] font-semibold text-[#1a1a1a]">가족</h1>
      </div>

      <div className="flex flex-col gap-5 px-5 pb-10">
        <div className="relative overflow-hidden rounded-3xl bg-[#1c1c1c] p-5">
          <div className="flex items-center gap-3.5">
            <div className="flex size-[52px] items-center justify-center rounded-full border-2 border-remine-pink bg-[#fff7cc] text-xl">👩</div>
            <div className="flex-1">
              <p className="text-[18px] font-semibold text-white">윤정아님</p>
              <p className="flex items-center gap-1.5 text-[13px] text-white/45">
                <span className="size-1.5 rounded-full bg-remine-blue" /> 오전에 산책 다녀오셨어요 🌿
              </p>
            </div>
            <span className="flex items-center gap-1 rounded-full bg-remine-blue/15 px-3 py-1.5 text-[12px] font-semibold text-remine-blue">
              인정
            </span>
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
        </div>

        <div className="flex flex-col gap-3 rounded-[20px] border border-[#ebebeb] bg-white p-5">
          <div className="flex items-center justify-between">
            <span className="text-[16px] font-semibold text-[#1a1a1a]">최근 대화</span>
            <Link to="/child/family/message" className="text-[13px] text-remine-blue">
              전체 보기 ›
            </Link>
          </div>
          {RECENT_CHAT.map((m, i) => (
            <div key={i} className="flex items-start gap-2.5">
              <span className="flex size-7 shrink-0 items-center justify-center rounded-full bg-[#fff7cc] text-[13px]">
                {m.from === 'them' ? '👩' : '👧'}
              </span>
              <div>
                <p className="text-[14px] leading-[1.4] text-[#1a1a1a]">{m.text}</p>
                <p className="text-[12px] text-[#9a9c91]">{m.time}</p>
              </div>
            </div>
          ))}
        </div>

        <div className="flex flex-col gap-3">
          <div className="flex items-baseline justify-between">
            <h2 className="text-[16px] font-semibold text-[#1a1a1a]">공유한 사진</h2>
            <span className="text-[13px] text-[#9a9c91]">어머니 퀴즈에 활용돼요</span>
          </div>
          <div className="flex gap-3">
            {SHARED_PHOTOS.map((p) => (
              <div key={p.label} className="w-[110px] shrink-0 overflow-hidden rounded-2xl bg-[#f2f2ee]">
                <img src={p.photo} alt={p.label} className="h-[90px] w-full object-cover" />
                <p className="px-2.5 py-2 text-[12px] text-[#1a1a1a]">{p.label}</p>
              </div>
            ))}
            <Link
              to="/child/memories/add"
              className="flex w-[110px] shrink-0 flex-col items-center justify-center gap-1 rounded-2xl border border-dashed border-[#ebebeb] text-remine-pink"
            >
              <span className="text-xl">＋</span>
              <span className="text-[12px] text-[#9a9c91]">추가</span>
            </Link>
          </div>
        </div>

        <div className="flex items-center justify-around rounded-3xl border border-[#ebebeb] bg-white py-4">
          <div className="flex flex-col items-center gap-0.5">
            <span className="text-[20px] font-semibold text-remine-pink">12개</span>
            <span className="text-[12px] text-[#9a9c91]">메시지</span>
          </div>
          <div className="flex flex-col items-center gap-0.5">
            <span className="text-[20px] font-semibold text-[#ffb84d]">4장</span>
            <span className="text-[12px] text-[#9a9c91]">공유 사진</span>
          </div>
          <div className="flex flex-col items-center gap-0.5">
            <span className="text-[20px] font-semibold text-remine-blue">3회</span>
            <span className="text-[12px] text-[#9a9c91]">통화</span>
          </div>
        </div>
      </div>
    </Screen>
  )
}
