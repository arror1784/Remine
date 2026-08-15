type ProgressDotsProps = {
  total: number
  current: number
  accentColor: string
}

export default function ProgressDots({ total, current, accentColor }: ProgressDotsProps) {
  return (
    <div className="flex w-full items-start justify-center gap-1.5">
      {Array.from({ length: total }).map((_, i) =>
        i === current ? (
          <div key={i} className="h-[7px] w-5 rounded-full" style={{ backgroundColor: accentColor }} />
        ) : (
          <div key={i} className="size-[7px] rounded-full bg-[#e0e0d8]" />
        ),
      )}
    </div>
  )
}
