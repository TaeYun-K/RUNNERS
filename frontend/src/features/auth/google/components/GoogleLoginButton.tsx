import { useGoogleLoginButton } from '../hooks/useGoogleLoginButton'
import type { GoogleLoginResponse } from '../types'

export function GoogleLoginButton(props: {
  onSuccess: (result: GoogleLoginResponse) => void
  onError?: (message: string | null) => void
}) {
  const { googleButtonRef, ready, loading, error } = useGoogleLoginButton({
    onSuccess: props.onSuccess,
    onError: props.onError,
  })

  return (
    <div
      className="grid justify-center"
      style={loading ? { pointerEvents: 'none', opacity: 0.7 } : undefined}
    >
      <div ref={googleButtonRef} />
      {!ready && !error ? (
        <p className="mt-3 text-center text-xs text-muted-foreground">로딩 중…</p>
      ) : null}
    </div>
  )
}

