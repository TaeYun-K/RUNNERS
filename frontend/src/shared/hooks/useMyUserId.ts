import { useCallback, useEffect, useState } from 'react'
import { apiFetch } from '../api/apiFetch'

type MeResponse = {
  userId: number
}

export function useMyUserId(enabled: boolean) {
  const [userId, setUserId] = useState<number | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const refresh = useCallback(
    async (signal?: AbortSignal) => {
      if (!enabled) {
        setUserId(null)
        setError(null)
        setLoading(false)
        return
      }

      setError(null)
      setLoading(true)
      try {
        const res = await apiFetch('/api/users/me', { signal })
        const json = (await res.json()) as Partial<MeResponse>
        if (signal?.aborted) return
        setUserId(typeof json.userId === 'number' ? json.userId : null)
      } catch (e) {
        if (!signal?.aborted) setError(e instanceof Error ? e.message : String(e))
      } finally {
        if (!signal?.aborted) setLoading(false)
      }
    },
    [enabled],
  )

  useEffect(() => {
    const controller = new AbortController()
    void refresh(controller.signal)
    return () => controller.abort()
  }, [refresh])

  return { userId, loading, error, refresh }
}

