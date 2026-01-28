import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { GoogleLoginButton, useAuth } from '../../features/auth'

export function LoginPage() {
  const { setAccessToken } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [error, setError] = useState<string | null>(null)

  const from = (location.state as { from?: string } | null)?.from ?? '/'

  return (
    <section className="grid min-h-screen place-items-center bg-background px-6 py-10">
      <div className="w-full max-w-md rounded-2xl border border-border bg-card p-6 shadow-sm">
        <h1 className="text-2xl font-bold tracking-tight text-foreground">로그인</h1>
        <p className="mt-2 text-sm leading-relaxed text-muted-foreground">
          Google 계정으로 로그인합니다.
        </p>

        <div className="mt-6 grid gap-4">
          {error ? (
            <div className="rounded-xl border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive">
              {error}
            </div>
          ) : null}

          <GoogleLoginButton
            onError={setError}
            onSuccess={(result) => {
              setAccessToken(result.accessToken)
              navigate(from, { replace: true })
            }}
          />
        </div>
      </div>
    </section>
  )
}
