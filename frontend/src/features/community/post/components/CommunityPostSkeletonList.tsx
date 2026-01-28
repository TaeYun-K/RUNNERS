export function CommunityPostSkeletonList(props: { count?: number }) {
  const count = props.count ?? 6

  return (
    <div className="grid gap-4">
      {Array.from({ length: count }).map((_, i) => (
        <div
          key={`skeleton-${i}`}
          className="grid gap-3 rounded-2xl border border-border bg-background p-5"
        >
          <div className="h-4 w-24 animate-pulse rounded bg-secondary" />
          <div className="h-5 w-2/3 animate-pulse rounded bg-secondary" />
          <div className="h-4 w-full animate-pulse rounded bg-secondary" />
          <div className="h-4 w-1/2 animate-pulse rounded bg-secondary" />
        </div>
      ))}
    </div>
  )
}

