import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, RefreshCw, Route, Timer, TrendingUp, User } from 'lucide-react'
import { useUserPublicProfile } from '../../features/MyPage/hooks/useUserPublicProfile'
import { NotFoundPage } from '../Error/NotFoundPage'

function formatMinutes(totalMinutes: number) {
  const minutes = Math.max(0, Math.floor(totalMinutes))
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h <= 0) return `${m}분`
  return `${h}시간 ${m}분`
}

function formatPaceMinutesPerKm(paceMinutesPerKm: number) {
  if (!Number.isFinite(paceMinutesPerKm) || paceMinutesPerKm <= 0) return '-'
  const totalSeconds = Math.round(paceMinutesPerKm * 60)
  const mm = Math.floor(totalSeconds / 60)
  const ss = totalSeconds % 60
  return `${mm}:${String(ss).padStart(2, '0')}/km`
}

export function UserPublicProfilePage() {
  const params = useParams()
  const userId = useMemo(() => {
    const raw = params.userId ?? ''
    const parsed = Number.parseInt(raw, 10)
    return Number.isFinite(parsed) ? parsed : null
  }, [params.userId])

  const { profile, error, loading, refresh, derived } = useUserPublicProfile(userId)
  const [isProfileImageOpen, setIsProfileImageOpen] = useState(false)

  if (userId == null) return <NotFoundPage />

  useEffect(() => {
    if (!isProfileImageOpen) return

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setIsProfileImageOpen(false)
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [isProfileImageOpen])

  const initials = (() => {
    const base = (profile?.displayName ?? '').trim()
    return base ? base.slice(0, 2).toUpperCase() : '?'
  })()

  return (
    <section className="mx-auto max-w-5xl rounded-2xl border border-border bg-card p-6 shadow-sm">
      {isProfileImageOpen && profile?.picture ? (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4"
          role="dialog"
          aria-modal="true"
          aria-label="프로필 이미지 크게 보기"
          onClick={() => setIsProfileImageOpen(false)}
        >
          <div
            className="max-h-[85vh] max-w-[92vw] overflow-hidden rounded-2xl bg-background shadow-xl"
            onClick={(e) => e.stopPropagation()}
          >
            <img
              src={profile.picture}
              alt="프로필"
              className="block max-h-[85vh] max-w-[92vw] object-contain"
              referrerPolicy="no-referrer"
            />
          </div>
        </div>
      ) : null}

      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-3">
          <Link
            to="/community"
            className="inline-flex h-9 items-center gap-2 rounded-full border border-border bg-background px-4 text-sm font-medium text-foreground transition hover:bg-secondary/60"
          >
            <ArrowLeft className="h-4 w-4" />
            커뮤니티
          </Link>
          <h2 className="text-xl font-bold tracking-tight text-foreground">
            사용자 프로필
          </h2>
        </div>

        <button
          type="button"
          onClick={() => void refresh()}
          className="inline-flex h-9 items-center gap-2 rounded-full border border-border bg-background px-4 text-sm font-medium text-foreground transition hover:bg-secondary/60 disabled:opacity-60"
          disabled={loading}
          aria-label="새로고침"
        >
          <RefreshCw className={loading ? 'h-4 w-4 animate-spin' : 'h-4 w-4'} />
          새로고침
        </button>
      </div>

      {error ? (
        <div className="mt-4 rounded-xl border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {error}
        </div>
      ) : null}

      <div className="mt-6 grid gap-6 lg:grid-cols-[320px_1fr]">
        <div className="overflow-hidden rounded-2xl border border-border bg-background">
          <div className="h-20 bg-gradient-to-r from-blue-600/15 to-blue-900/10" />
          <div className="p-5">
            <div className="-mt-12 flex items-end gap-4">
              {profile?.picture ? (
                <button
                  type="button"
                  onClick={() => setIsProfileImageOpen(true)}
                  className="rounded-2xl"
                  aria-label="프로필 이미지 크게 보기"
                >
                  <img
                    src={profile.picture}
                    alt="프로필"
                    className="h-24 w-24 rounded-2xl border-4 border-card object-cover shadow-md transition hover:opacity-90"
                    referrerPolicy="no-referrer"
                  />
                </button>
              ) : (
                <div className="flex h-24 w-24 items-center justify-center rounded-2xl border-4 border-card bg-secondary text-xl font-bold text-muted-foreground shadow-md">
                  {initials}
                </div>
              )}
              <div className="min-w-0 pb-2">
                {loading && !profile ? (
                  <div className="grid gap-2">
                    <div className="h-5 w-40 animate-pulse rounded bg-secondary" />
                    <div className="h-4 w-28 animate-pulse rounded bg-secondary" />
                  </div>
                ) : (
                  <>
                    <p className="truncate text-lg font-bold text-foreground">
                      {profile?.displayName ?? 'RUNNERS'}
                    </p>
                    <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
                      {profile?.intro || '아직 소개가 없어요.'}
                    </p>
                  </>
                )}
              </div>
            </div>

            <div className="mt-5 rounded-xl border border-border bg-card p-4">
              <div className="flex items-center gap-2 text-sm font-semibold text-foreground">
                <User className="h-4 w-4 text-blue-600" />
                기본 정보
              </div>
              <p className="mt-2 text-sm text-muted-foreground">
                userId: {userId}
              </p>
            </div>
          </div>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <div className="rounded-2xl border border-border bg-background p-5">
            <div className="flex items-center justify-between">
              <p className="text-sm font-semibold text-foreground">누적 거리</p>
              <Route className="h-5 w-5 text-blue-600" />
            </div>
            {loading && !profile ? (
              <div className="mt-3 h-9 w-32 animate-pulse rounded bg-secondary" />
            ) : (
              <p className="mt-3 text-3xl font-bold tracking-tight text-foreground">
                {typeof profile?.totalDistanceKm === 'number'
                  ? `${profile.totalDistanceKm.toFixed(1)} km`
                  : '-'}
              </p>
            )}
            <p className="mt-2 text-xs text-muted-foreground">
              지금까지 달린 총 거리
            </p>
          </div>

          <div className="rounded-2xl border border-border bg-background p-5">
            <div className="flex items-center justify-between">
              <p className="text-sm font-semibold text-foreground">누적 시간</p>
              <Timer className="h-5 w-5 text-blue-600" />
            </div>
            {loading && !profile ? (
              <div className="mt-3 h-9 w-40 animate-pulse rounded bg-secondary" />
            ) : (
              <p className="mt-3 text-3xl font-bold tracking-tight text-foreground">
                {typeof profile?.totalDurationMinutes === 'number'
                  ? formatMinutes(profile.totalDurationMinutes)
                  : '-'}
              </p>
            )}
            <p className="mt-2 text-xs text-muted-foreground">총 운동 시간</p>
          </div>

          <div className="rounded-2xl border border-border bg-background p-5">
            <div className="flex items-center justify-between">
              <p className="text-sm font-semibold text-foreground">러닝 횟수</p>
              <User className="h-5 w-5 text-blue-600" />
            </div>
            {loading && !profile ? (
              <div className="mt-3 h-9 w-24 animate-pulse rounded bg-secondary" />
            ) : (
              <p className="mt-3 text-3xl font-bold tracking-tight text-foreground">
                {typeof profile?.runCount === 'number'
                  ? `${profile.runCount.toLocaleString()}회`
                  : '-'}
              </p>
            )}
            <p className="mt-2 text-xs text-muted-foreground">
              운동 기록 누적 횟수
            </p>
          </div>

          <div className="rounded-2xl border border-border bg-background p-5">
            <div className="flex items-center justify-between">
              <p className="text-sm font-semibold text-foreground">평균 페이스</p>
              <TrendingUp className="h-5 w-5 text-blue-600" />
            </div>
            {loading && !profile ? (
              <div className="mt-3 h-9 w-28 animate-pulse rounded bg-secondary" />
            ) : (
              <p className="mt-3 text-3xl font-bold tracking-tight text-foreground">
                {derived.avgPace != null ? formatPaceMinutesPerKm(derived.avgPace) : '-'}
              </p>
            )}
            <p className="mt-2 text-xs text-muted-foreground">
              누적 기준 평균(min/km)
            </p>
          </div>

          <div className="sm:col-span-2 rounded-2xl border border-border bg-background p-5">
            <p className="text-sm font-semibold text-foreground">요약</p>
            <div className="mt-3 grid gap-2 text-sm text-muted-foreground">
              <p>
                평균 거리:{' '}
                {derived.avgDistancePerRun != null
                  ? `${derived.avgDistancePerRun.toFixed(1)} km/회`
                  : '-'}
              </p>
              <p>
                평균 페이스:{' '}
                {derived.avgPace != null ? formatPaceMinutesPerKm(derived.avgPace) : '-'}
              </p>
              <p className="text-xs text-muted-foreground/80">
                * 이 화면은 공개 프로필(`/users/{'{userId}'}/public-profile`) 기준입니다.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
