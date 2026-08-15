import BottomTabBar from '@/components/BottomTabBar'

type PlaceholderProps = {
  role: 'parent' | 'child'
  accentColor: string
  label: string
}

export default function Placeholder({ role, accentColor, label }: PlaceholderProps) {
  return (
    <div className="relative flex min-h-screen w-full items-center justify-center bg-remine-bg px-6 pb-24 text-center">
      <p className="text-[15px] leading-[1.6] text-[#9a9c91]">{label} 화면은 다음 단계에서 구현됩니다.</p>
      <BottomTabBar role={role} accentColor={accentColor} />
    </div>
  )
}
