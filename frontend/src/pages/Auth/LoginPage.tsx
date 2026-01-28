'use client'

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
    <section className="min-h-[calc(100vh-8rem)] bg-background px-4">
      <div className="flex min-h-[calc(100vh-8rem)] items-center justify-center">
        <div className="mx-auto w-full max-w-md translate-y-6">
          <div className="rounded-2xl border border-border/60 bg-card/60 p-8 shadow-sm backdrop-blur">
            <div className="flex flex-col items-center text-center">
              <div className="flex h-24 w-24 items-center justify-center rounded-3xl bg-blue-50 dark:bg-blue-950/30">
                <img
                  src="/runners_icon_512.png"
                  alt="RUNNERS Logo"
                  className="h-12 w-auto"
                />
              </div>

              <div className="mt-7 space-y-2">
                <h1 className="text-3xl font-extrabold tracking-tight text-foreground">
                  RUNNERS
                </h1>
                <p className="text-balance text-sm leading-relaxed text-muted-foreground">
                  러너들의 공간에 오신 것을 환영합니다.
                  <br />
                  Google 계정으로 간편하게 시작하세요.
                </p>
              </div>
            </div>
          </div>

          <div className="mt-8">
            {error ? (
              <div className="mb-4 rounded-lg border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive">
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

            <p className="mt-6 text-center text-xs leading-relaxed text-muted-foreground">
              로그인 시{' '}
              <a href="/terms" className="underline underline-offset-2 hover:text-foreground">
                이용약관
              </a>{' '}
              및{' '}
              <a href="/privacy" className="underline underline-offset-2 hover:text-foreground">
                개인정보처리방침
              </a>
              에 동의하게 됩니다.
            </p>
          </div>
        </div>
      </div>
    </section>
  )
}
