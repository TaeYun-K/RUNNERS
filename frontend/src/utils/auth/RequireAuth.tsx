import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from './AuthProvider'

export function RequireAuth({ children }: { children: ReactNode }) {
  const { accessToken, bootstrapping } = useAuth()
  const location = useLocation()

  if (bootstrapping) {
    return (
      <section className="mx-auto max-w-4xl rounded-2xl border border-border bg-card p-6 shadow-sm">
        <p className="text-sm text-muted-foreground">인증 상태 확인 중…</p>
      </section>
    )
  }

  if (!accessToken) {
    return (
      <Navigate
        to="/login"
        replace
        state={{ from: `${location.pathname}${location.search}${location.hash}` }}
      />
    )
  }

  return children
}
