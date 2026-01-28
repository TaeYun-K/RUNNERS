import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { fetchMyProfile } from '../api/profile'
import { fetchUserPublicProfile } from '../api/publicProfile'
import type { MyProfile, UserPublicProfile } from '../types'

export function useMyDashboard() {
  const [me, setMe] = useState<MyProfile | null>(null)
  const [publicProfile, setPublicProfile] = useState<UserPublicProfile | null>(
    null,
  )
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const mountedRef = useRef(false)

  const load = useCallback(async () => {
    if (!mountedRef.current) return
    setLoading(true)
    setError(null)
    try {
      const meRes = await fetchMyProfile()
      if (!mountedRef.current) return
      setMe(meRes)
      const publicRes = await fetchUserPublicProfile(meRes.userId)
      if (!mountedRef.current) return
      setPublicProfile(publicRes)
    } catch (e) {
      if (!mountedRef.current) throw e
      setError(e instanceof Error ? e.message : String(e))
    } finally {
      if (mountedRef.current) setLoading(false)
    }
  }, [])

  useEffect(() => {
    mountedRef.current = true
    void load().catch(() => {})
    return () => {
      mountedRef.current = false
    }
  }, [load])

  const stats = useMemo(() => {
    const totalDistanceKm = publicProfile?.totalDistanceKm ?? null
    const totalDurationMinutes = publicProfile?.totalDurationMinutes ?? null
    const runCount = publicProfile?.runCount ?? null
    return { totalDistanceKm, totalDurationMinutes, runCount }
  }, [publicProfile])

  return {
    me,
    publicProfile,
    loading,
    error,
    refresh: load,
    stats,
  }
}

