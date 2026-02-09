export function formatRelativeTime(iso: string) {
  const created = new Date(iso)
  const now = new Date()
  const diffMs = now.getTime() - created.getTime()
  const seconds = Math.floor(diffMs / 1000)

  if (seconds < 60) return '방금 전'

  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return `${minutes}분 전`

  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}시간 전`

  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}일 전`

  return created.toLocaleDateString('ko-KR', {
    month: 'short',
    day: 'numeric',
  })
}
