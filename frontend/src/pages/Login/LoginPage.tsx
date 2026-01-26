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
    <section className="auth">
      <div className="card auth-card">
        <h1>로그인</h1>
        <p className="muted">
          우선 개발 편의를 위해 `POST /api/auth/dev/token` 기반으로만 붙여뒀어요.
        </p>

        <form className="form" onSubmit={handleSubmit}>
          <label className="field">
            <span>이메일</span>
            <input
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
              required
            />
          </label>

          <label className="field">
            <span>이름(옵션)</span>
            <input value={name} onChange={(e) => setName(e.target.value)} />
          </label>

          {error ? <div className="error">{error}</div> : null}

          <button className="btn btn-primary" disabled={loading} type="submit">
            {loading ? '로그인 중…' : '로그인'}
          </button>
        </form>
      </div>
    </section>
  )
}
