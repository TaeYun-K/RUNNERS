import { useEffect, useMemo, useRef, useState } from 'react'
import type { ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  clearAccessToken,
  getAccessToken,
  setAccessToken,
  subscribeAccessToken,
} from '../../../../shared/auth/token'
import { subscribeAuthEvent } from '../../../../shared/auth/authEvents'
import { refreshAccessToken } from '../api/refreshAccessToken'
import { AuthContext, type AuthContextValue } from './AuthContext'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [accessTokenState, setAccessTokenState] = useState<string | null>(() =>
    getAccessToken(),
  )
  const [bootstrapping, setBootstrapping] = useState(true)
  const logoutAlertShownRef = useRef(false)
  const navigate = useNavigate()

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        if (getAccessToken()) return
        const refreshed = await refreshAccessToken()
        if (!refreshed) return
        setAccessToken(refreshed)
      } finally {
        if (!cancelled) setBootstrapping(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    return subscribeAccessToken((token) => setAccessTokenState(token))
  }, [])

  useEffect(() => {
    if (accessTokenState) logoutAlertShownRef.current = false
  }, [accessTokenState])

  useEffect(() => {
    return subscribeAuthEvent((event) => {
      if (event.type !== 'logged_out') return
      if (logoutAlertShownRef.current) return
      logoutAlertShownRef.current = true
      if (event.reason === 'user_logout') {
        alert('로그아웃되었습니다.')
      } else if (event.reason === 'session_expired') {
        alert('세션이 만료되어 로그아웃되었습니다. 다시 로그인해주세요.')
      } else {
        alert(
          '다른 기기/브라우저에서 로그인 또는 로그아웃되어 로그아웃되었습니다. 다시 로그인해주세요.',
        )
      }
      navigate('/', { replace: true })
    })
  }, [navigate])

  const value = useMemo<AuthContextValue>(
    () => ({
      accessToken: accessTokenState,
      bootstrapping,
      setAccessToken: (token: string) => {
        setAccessToken(token)
      },
      clearAccessToken: () => {
        clearAccessToken()
      },
    }),
    [accessTokenState, bootstrapping],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
