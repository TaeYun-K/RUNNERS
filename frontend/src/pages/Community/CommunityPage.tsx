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
    <section className="card">
      <h2>커뮤니티</h2>
      <p className="muted">우선은 목록 조회만 붙여둔 상태예요.</p>
      {error ? <div className="error">{error}</div> : null}
      <pre className="code">{JSON.stringify(data, null, 2)}</pre>
    </section>
  )
}

