import { useCallback, useEffect, useMemo, useState } from 'react'
import { fetchUserPublicProfile } from '../api/publicProfile'
import type { UserPublicProfile } from '../types'

export function useUserPublicProfile(userId: number | null) {
  const [profile, setProfile] = useState<UserPublicProfile | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const refresh = useCallback(
    async (signal?: AbortSignal) => {
      if (userId == null || !Number.isFinite(userId)) {
        setProfile(null)
        setError(null)
        setLoading(false)
        return
      }

      setError(null)
      setLoading(true)
      try {
        const json = await fetchUserPublicProfile(userId)
        if (signal?.aborted) return
        setProfile(json)
      } catch (e) {
        if (!signal?.aborted) setError(e instanceof Error ? e.message : String(e))
      } finally {
        if (!signal?.aborted) setLoading(false)
      }
    },
    [userId],
  )

  useEffect(() => {
    const controller = new AbortController()
    void refresh(controller.signal)
    return () => controller.abort()
  }, [refresh])

  const derived = useMemo(() => {
    const distance = profile?.totalDistanceKm ?? null
    const duration = profile?.totalDurationMinutes ?? null
    const runs = profile?.runCount ?? null

    const avgPace =
      typeof distance === 'number' && distance > 0 && typeof duration === 'number'
        ? duration / distance
        : null
    const avgDistancePerRun =
      typeof distance === 'number' &&
      distance > 0 &&
      typeof runs === 'number' &&
      runs > 0
        ? distance / runs
        : null

    return { avgPace, avgDistancePerRun }
  }, [profile?.runCount, profile?.totalDistanceKm, profile?.totalDurationMinutes])

  return { profile, error, loading, refresh, derived }
}

