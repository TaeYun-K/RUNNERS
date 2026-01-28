import { useEffect, useState } from 'react'
import { apiFetch } from '../../shared/api/apiFetch'

export function DashboardPage() {
  const [me, setMe] = useState<unknown>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        setError(null)
        const res = await apiFetch('/api/users/me')
        const json = (await res.json()) as unknown
        if (!cancelled) setMe(json)
      } catch (e) {
        if (!cancelled) setError(e instanceof Error ? e.message : String(e))
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <section className="grid gap-6 lg:grid-cols-2">
      <div className="rounded-2xl border border-border bg-card p-6 shadow-sm">
        <h2 className="text-xl font-bold tracking-tight text-foreground">내 정보</h2>
        <p className="mt-2 text-sm text-muted-foreground">
          백엔드 `GET /users/me`를 호출해 화면 뼈대를 확인합니다.
        </p>
        {error ? (
          <div className="mt-4 rounded-xl border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive">
            {error}
          </div>
        ) : null}
        <pre className="mt-4 overflow-auto rounded-xl bg-slate-900 px-4 py-3 text-xs leading-relaxed text-slate-50/90">
          {JSON.stringify(me, null, 2)}
        </pre>
      </div>

      <div className="rounded-2xl border border-border bg-card p-6 shadow-sm">
        <h2 className="text-xl font-bold tracking-tight text-foreground">다음 단계</h2>
        <ul className="mt-4 list-disc space-y-1 pl-5 text-sm text-muted-foreground">
          <li>여기에 대시보드 카드 UI를 붙입니다.</li>
          <li>API 응답 타입을 정의하고 컴포넌트를 분리합니다.</li>
          <li>커뮤니티/러닝기록/프로필 화면을 확장합니다.</li>
        </ul>
      </div>
    </section>
  )
}

