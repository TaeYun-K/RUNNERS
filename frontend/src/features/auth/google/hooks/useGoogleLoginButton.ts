import { useEffect, useRef, useState } from 'react'
import { loadGoogleIdentityServices } from '../../../../shared/auth/googleIdentity'
import { googleLogin } from '../api/googleLogin'
import type { GoogleLoginResponse } from '../types'

export function useGoogleLoginButton(params: {
  onSuccess: (result: GoogleLoginResponse) => void
  onError?: (message: string | null) => void
}) {
  const googleButtonRef = useRef<HTMLDivElement | null>(null)
  const onSuccessRef = useRef(params.onSuccess)
  const onErrorRef = useRef(params.onError)

  const [ready, setReady] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    onSuccessRef.current = params.onSuccess
  }, [params.onSuccess])

  useEffect(() => {
    onErrorRef.current = params.onError
  }, [params.onError])

  useEffect(() => {
    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID
    if (!clientId) {
      const message = '환경변수 VITE_GOOGLE_CLIENT_ID 가 설정되지 않았어요.'
      setError(message)
      onErrorRef.current?.(message)
      return
    }

    let cancelled = false
    ;(async () => {
      try {
        await loadGoogleIdentityServices()
        if (cancelled) return

        if (!window.google?.accounts?.id) {
          throw new Error('Google Identity Services 초기화에 실패했어요.')
        }

        window.google.accounts.id.initialize({
          client_id: clientId,
          callback: async ({ credential }: { credential?: string }) => {
            if (!credential) {
              const message = 'Google 로그인 응답이 올바르지 않아요.'
              setError(message)
              onErrorRef.current?.(message)
              return
            }

            setError(null)
            onErrorRef.current?.(null)
            setLoading(true)
            try {
              const result = await googleLogin(credential)
              onSuccessRef.current(result)
            } catch (e) {
              const message = e instanceof Error ? e.message : String(e)
              setError(message)
              onErrorRef.current?.(message)
            } finally {
              setLoading(false)
            }
          },
        })

        if (googleButtonRef.current) {
          googleButtonRef.current.innerHTML = ''
          window.google.accounts.id.renderButton(googleButtonRef.current, {
            theme: 'outline',
            size: 'large',
            shape: 'pill',
            width: 320,
            text: 'signin_with',
          })
        }

        setReady(true)
      } catch (e) {
        const message = e instanceof Error ? e.message : String(e)
        setError(message)
        onErrorRef.current?.(message)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [])

  return { googleButtonRef, ready, loading, error }
}

