import { useState } from 'react'
import type { FormEvent } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../utils/auth/AuthProvider'
import { apiFetch } from '../../utils/api/apiFetch'

type DevTokenResponse = {
  accessToken: string
}

export function LoginPage() {
  const { setAccessToken } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [email, setEmail] = useState('dev@runners.local')
  const [name, setName] = useState('Dev User')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const from = (location.state as { from?: string } | null)?.from ?? '/'

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const res = await apiFetch('/api/auth/dev/token', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, name, role: 'USER' }),
      })

      const json = (await res.json()) as DevTokenResponse
      setAccessToken(json.accessToken)
      navigate(from, { replace: true })
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="grid min-h-screen place-items-center bg-background px-6 py-10">
      <div className="w-full max-w-md rounded-2xl border border-border bg-card p-6 shadow-sm">
        <h1 className="text-2xl font-bold tracking-tight text-foreground">로그인</h1>
        <p className="mt-2 text-sm leading-relaxed text-muted-foreground">
          우선 개발 편의를 위해 `POST /api/auth/dev/token` 기반으로만 붙여뒀어요.
        </p>

        <form className="mt-6 grid gap-4" onSubmit={handleSubmit}>
          <label className="grid gap-2">
            <span className="text-sm font-medium text-foreground">이메일</span>
            <input
              className="h-11 rounded-xl border border-border bg-background px-4 text-sm text-foreground outline-none transition focus:border-blue-500/40 focus:ring-2 focus:ring-blue-500/20"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
              required
            />
          </label>

          <label className="grid gap-2">
            <span className="text-sm font-medium text-foreground">이름(옵션)</span>
            <input
              className="h-11 rounded-xl border border-border bg-background px-4 text-sm text-foreground outline-none transition focus:border-blue-500/40 focus:ring-2 focus:ring-blue-500/20"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </label>

          {error ? (
            <div className="rounded-xl border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive">
              {error}
            </div>
          ) : null}

          <button
            className="h-11 rounded-xl bg-blue-600 px-4 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
            disabled={loading}
            type="submit"
          >
            {loading ? '로그인 중…' : '로그인'}
          </button>
        </form>
      </div>
    </section>
  )
}
