/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import {
  clearAccessToken,
  getAccessToken,
  setAccessToken,
} from '../../shared/auth/token'

type AuthContextValue = {
  accessToken: string | null
  setAccessToken: (token: string) => void
  clearAccessToken: () => void
  bootstrapping: boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

async function tryRefresh() {
  const res = await fetch('/api/auth/refresh', {
    method: 'POST',
    credentials: 'include',
  })
  if (!res.ok) return null
  const json = (await res.json()) as { accessToken?: string }
  return json.accessToken ?? null
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [accessTokenState, setAccessTokenState] = useState<string | null>(() =>
    getAccessToken(),
  )
  const [bootstrapping, setBootstrapping] = useState(true)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        if (getAccessToken()) return
        const refreshed = await tryRefresh()
        if (!refreshed) return
        setAccessToken(refreshed)
        if (!cancelled) setAccessTokenState(refreshed)
      } finally {
        if (!cancelled) setBootstrapping(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      accessToken: accessTokenState,
      bootstrapping,
      setAccessToken: (token) => {
        setAccessToken(token)
        setAccessTokenState(token)
      },
      clearAccessToken: () => {
        clearAccessToken()
        setAccessTokenState(null)
      },
    }),
    [accessTokenState, bootstrapping],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('AuthProvider is missing')
  return ctx
}

