import { useEffect, useState } from 'react'
import { apiFetch } from '../../utils/api/apiFetch'

export function CommunityPage() {
  const [data, setData] = useState<unknown>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        setError(null)
        const res = await apiFetch('/api/community/posts?size=10')
        const json = (await res.json()) as unknown
        if (!cancelled) setData(json)
      } catch (e) {
        if (!cancelled) setError(e instanceof Error ? e.message : String(e))
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <section className="mx-auto max-w-4xl rounded-2xl border border-border bg-card p-6 shadow-sm">
      <h2 className="text-xl font-bold tracking-tight text-foreground">커뮤니티</h2>
      <p className="mt-2 text-sm text-muted-foreground">
        우선은 목록 조회만 붙여둔 상태예요.
      </p>
      {error ? (
        <div className="mt-4 rounded-xl border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {error}
        </div>
      ) : null}
      <pre className="mt-4 overflow-auto rounded-xl bg-slate-900 px-4 py-3 text-xs leading-relaxed text-slate-50/90">
        {JSON.stringify(data, null, 2)}
      </pre>
    </section>
  )
}
