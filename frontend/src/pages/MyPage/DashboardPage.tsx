import { useMemo } from 'react'
import { Activity, RefreshCw, Route, Timer, TrendingUp } from 'lucide-react'
import { useMyDashboard } from '../../features/MyPage/hooks/useMyDashboard'

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

export function DashboardPage() {
  const { publicProfile, loading, error, refresh, stats } = useMyDashboard()

  const derived = useMemo(() => {
    const distance = stats.totalDistanceKm ?? 0
    const duration = stats.totalDurationMinutes ?? 0
    const runs = stats.runCount ?? 0

    const avgPace = distance > 0 && duration > 0 ? duration / distance : null
    const avgDistancePerRun = distance > 0 && runs > 0 ? distance / runs : null
    return { avgPace, avgDistancePerRun }
  }, [stats.runCount, stats.totalDistanceKm, stats.totalDurationMinutes])

  const initials = (() => {
    const base = (publicProfile?.displayName ?? '').trim()
    return base ? base.slice(0, 2).toUpperCase() : '?'
  })()

  return (
    <section className="mx-auto max-w-5xl">
      <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-foreground">
            내 헬스 대시보드
          </h2>
          <p className="mt-2 text-sm text-muted-foreground">
            누적 거리/시간/횟수 등 러닝 요약을 확인합니다.<br></br>
            어플에서 Health 데이터 동기화를 활성화해야 정확한 통계가
            집계됩니다.
          </p>
        </div>

        <button
          type="button"
          onClick={() => void refresh()}
          disabled={loading}
          className="inline-flex h-10 items-center justify-center gap-2 rounded-xl border border-border bg-background px-4 text-sm font-semibold text-foreground transition hover:bg-secondary/60 disabled:cursor-not-allowed disabled:opacity-60"
        >
          <RefreshCw className={loading ? 'h-4 w-4 animate-spin' : 'h-4 w-4'} />
          새로고침
        </button>
      </div>

      {error ? (
        <div className="mb-6 rounded-xl border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {error}
        </div>
      ) : null}

      <div className="grid gap-6 lg:grid-cols-[320px_1fr]">
        <div className="overflow-hidden rounded-2xl border border-border bg-card shadow-sm">
          <div className="h-20 bg-gradient-to-r from-blue-600/15 to-blue-900/10" />
          <div className="p-5">
            <div className="-mt-12 flex items-end gap-4">
              {publicProfile?.picture ? (
                <img
                  src={publicProfile.picture}
                  alt="프로필"
                  className="h-24 w-24 rounded-2xl border-4 border-card object-cover shadow-md"
                  referrerPolicy="no-referrer"
                />
              ) : (
                <div className="flex h-24 w-24 items-center justify-center rounded-2xl border-4 border-card bg-muted text-xl font-bold text-muted-foreground shadow-md">
                  {initials}
                </div>
              )}
              <div className="min-w-0 pb-2">
                {loading && !publicProfile ? (
                  <div className="grid gap-2">
                    <div className="h-5 w-40 animate-pulse rounded bg-secondary" />
                    <div className="h-4 w-28 animate-pulse rounded bg-secondary" />
                  </div>
                ) : (
                  <>
                    <p className="truncate text-lg font-bold text-foreground">
                      {publicProfile?.displayName ?? 'RUNNERS'}
                    </p>
                    <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
                      {publicProfile?.intro || '아직 소개가 없어요.'}
                    </p>
                  </>
                )}
              </div>
            </div>

            <div className="mt-5 grid gap-2 rounded-xl border border-border bg-background p-4 text-sm">
              <p className="font-semibold text-foreground">오늘의 한 줄</p>
              <p className="text-muted-foreground">
                꾸준함은 가장 강력한 스킬입니다.
              </p>
            </div>
          </div>
        </div>

        <div className="grid gap-6">
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="rounded-2xl border border-border bg-card p-5 shadow-sm">
              <div className="flex items-center justify-between">
                <p className="text-sm font-semibold text-foreground">누적 거리</p>
                <Route className="h-5 w-5 text-blue-600" />
              </div>
              {loading && !publicProfile ? (
                <div className="mt-3 h-9 w-32 animate-pulse rounded bg-secondary" />
              ) : (
                <p className="mt-3 text-3xl font-bold tracking-tight text-foreground">
                  {typeof stats.totalDistanceKm === 'number'
                    ? `${stats.totalDistanceKm.toFixed(1)} km`
                    : '-'}
                </p>
              )}
              <p className="mt-2 text-xs text-muted-foreground">
                지금까지 달린 총 거리
              </p>
            </div>

            <div className="rounded-2xl border border-border bg-card p-5 shadow-sm">
              <div className="flex items-center justify-between">
                <p className="text-sm font-semibold text-foreground">누적 시간</p>
                <Timer className="h-5 w-5 text-blue-600" />
              </div>
              {loading && !publicProfile ? (
                <div className="mt-3 h-9 w-40 animate-pulse rounded bg-secondary" />
              ) : (
                <p className="mt-3 text-3xl font-bold tracking-tight text-foreground">
                  {typeof stats.totalDurationMinutes === 'number'
                    ? formatMinutes(stats.totalDurationMinutes)
                    : '-'}
                </p>
              )}
              <p className="mt-2 text-xs text-muted-foreground">총 운동 시간</p>
            </div>

            <div className="rounded-2xl border border-border bg-card p-5 shadow-sm">
              <div className="flex items-center justify-between">
                <p className="text-sm font-semibold text-foreground">러닝 횟수</p>
                <Activity className="h-5 w-5 text-blue-600" />
              </div>
              {loading && !publicProfile ? (
                <div className="mt-3 h-9 w-24 animate-pulse rounded bg-secondary" />
              ) : (
                <p className="mt-3 text-3xl font-bold tracking-tight text-foreground">
                  {typeof stats.runCount === 'number'
                    ? `${stats.runCount.toLocaleString()}회`
                    : '-'}
                </p>
              )}
              <p className="mt-2 text-xs text-muted-foreground">
                운동 기록 누적 횟수
              </p>
            </div>

            <div className="rounded-2xl border border-border bg-card p-5 shadow-sm">
              <div className="flex items-center justify-between">
                <p className="text-sm font-semibold text-foreground">평균 페이스</p>
                <TrendingUp className="h-5 w-5 text-blue-600" />
              </div>
              {loading && !publicProfile ? (
                <div className="mt-3 h-9 w-28 animate-pulse rounded bg-secondary" />
              ) : (
                <p className="mt-3 text-3xl font-bold tracking-tight text-foreground">
                  {derived.avgPace != null
                    ? formatPaceMinutesPerKm(derived.avgPace)
                    : '-'}
                </p>
              )}
              <p className="mt-2 text-xs text-muted-foreground">
                누적 기준 평균(min/km)
              </p>
            </div>
          </div>

          <div className="rounded-2xl border border-border bg-card p-5 shadow-sm">
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
                * 평균 값은 누적 통계를 기반으로 계산합니다.
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}
