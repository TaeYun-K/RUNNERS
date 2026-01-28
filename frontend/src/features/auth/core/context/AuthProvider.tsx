import { useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import {
  clearAccessToken,
  getAccessToken,
  setAccessToken,
} from '../../../../shared/auth/token'
import { refreshAccessToken } from '../api/refreshAccessToken'
import { AuthContext, type AuthContextValue } from './AuthContext'

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
        const refreshed = await refreshAccessToken()
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
      setAccessToken: (token: string) => {
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

