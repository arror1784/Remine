import { Link, useLocation } from 'react-router-dom'
import Screen from '@/components/Screen'
import ModeBar from '@/components/ModeBar'
import BottomTabBar from '@/components/BottomTabBar'
import springOuting from '@/assets/memories/family-trip.png'
import birthdayCake from '@/assets/memories/birthday-cake.png'
import grandchildWalk from '@/assets/memories/grandchild-walk.png'

const PHOTOS = [
  { photo: springOuting, title: '2022년 여름 가족 여행', date: '2022년 7월', addedBy: '지영님 추가', status: '퀴즈 활용 중' },
  { photo: birthdayCake, title: '어머니 생신', date: '2025년 12월', addedBy: '민호님 추가', status: '퀴즈 활용 중' },
  { photo: grandchildWalk, title: '손주와 산책', date: '2025년 10월', addedBy: '지영님 추가', status: '대기 중' },
]

export default function ChildMemoryGallery() {
  const location = useLocation()

  return (
    <Screen footer={<BottomTabBar role="child" accentColor="#37ceff" />}>
      <ModeBar label="자녀 모드 — 지영님" color="#37ceff" dark />

      <div className="flex items-center justify-between px-5 py-3.5">
        <h1 className="text-[22px] font-semibold text-[#1a1a1a]">추억 앨범</h1>
        <Link
          to="/child/memories/add"
          state={{ backgroundLocation: location }}
          className="h-[38px] rounded-full bg-remine-blue px-4 text-[15px] font-semibold leading-[38px] text-white"
        >
          ＋ 사진 추가
        </Link>
      </div>

      <div className="flex flex-col gap-5 px-5 pb-10">
        <div className="flex gap-3 rounded-2xl bg-[#fff7cc] px-5 py-4">
          <span className="text-[15px]">🧩</span>
          <div>
            <p className="text-[15px] font-semibold text-[#1a1a1a]">어머니 퀴즈에 활용돼요</p>
            <p className="pt-1 text-[13px] leading-[1.5] text-[#66695d]">자녀분들이 추가한 사진이 어머니의 추억 퀴즈에 자동으로 반영돼요. 소중한 추억을 추가해 보세요.</p>
          </div>
        </div>

        <div className="grid grid-cols-3 gap-2.5">
          <div className="flex flex-col items-center gap-1 rounded-2xl border border-[#ebebeb] bg-white py-4">
            <span className="text-[22px] font-semibold text-remine-blue">3장</span>
            <span className="text-[13px] text-[#9a9c91]">총 사진</span>
          </div>
          <div className="flex flex-col items-center gap-1 rounded-2xl border border-[#ebebeb] bg-white py-4">
            <span className="text-[22px] font-semibold text-remine-pink">2장</span>
            <span className="text-[13px] text-[#9a9c91]">퀴즈 활용</span>
          </div>
          <div className="flex flex-col items-center gap-1 rounded-2xl border border-[#ebebeb] bg-white py-4">
            <span className="text-[22px] font-semibold text-[#ffb84d]">1장</span>
            <span className="text-[13px] text-[#9a9c91]">이번 달 추가</span>
          </div>
        </div>

        <div className="flex flex-col gap-3">
          <h2 className="text-[18px] font-semibold text-[#1a1a1a]">추가된 사진</h2>
          {PHOTOS.map((p) => (
            <div key={p.title} className="flex items-center gap-3.5 rounded-2xl border border-[#ebebeb] bg-white p-3">
              <img src={p.photo} alt={p.title} className="size-[68px] shrink-0 rounded-xl object-cover" />
              <div className="flex-1">
                <p className="text-[15px] font-medium text-[#1a1a1a]">{p.title}</p>
                <p className="text-[13px] text-[#9a9c91]">
                  {p.date} · {p.addedBy}
                </p>
              </div>
              <span
                className="shrink-0 rounded-full px-2.5 py-1 text-[12px] font-semibold"
                style={{
                  backgroundColor: p.status === '퀴즈 활용 중' ? '#e8f8ff' : '#f2f2ee',
                  color: p.status === '퀴즈 활용 중' ? '#37ceff' : '#9a9c91',
                }}
              >
                {p.status}
              </span>
            </div>
          ))}
        </div>

        <Link
          to="/child/memories/add"
          state={{ backgroundLocation: location }}
          className="flex h-[52px] items-center justify-center gap-2 rounded-2xl bg-[#1a1a1a] text-[16px] font-semibold text-white"
        >
          📷 새 추억 사진 추가하기
        </Link>
      </div>
    </Screen>
  )
}
