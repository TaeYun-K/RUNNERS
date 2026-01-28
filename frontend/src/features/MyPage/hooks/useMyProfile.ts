import { useCallback, useEffect, useRef, useState } from 'react'
import { fetchMyProfile, updateMyProfile } from '../api/profile'
import type { MyProfile, UpdateMyProfileRequest } from '../types'

export function useMyProfile() {
  const [profile, setProfile] = useState<MyProfile | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const mountedRef = useRef(false)

  const load = useCallback(async (opts?: { silent?: boolean }) => {
    try {
      if (!mountedRef.current) return null
      setError(null)
      if (!opts?.silent) setLoading(true)
      const me = await fetchMyProfile()
      if (!mountedRef.current) return null
      setProfile(me)
      return me
    } catch (e) {
      if (!mountedRef.current) throw e
      const message = e instanceof Error ? e.message : String(e)
      setError(message)
      throw e
    } finally {
      if (mountedRef.current && !opts?.silent) setLoading(false)
    }
  }, [])

  useEffect(() => {
    mountedRef.current = true
    void load().catch(() => {})
    return () => {
      mountedRef.current = false
    }
  }, [load])

  const refetch = useCallback(async () => {
    if (!mountedRef.current) return
    setLoading(true)
    try {
      await load({ silent: true })
    } finally {
      if (mountedRef.current) setLoading(false)
    }
  }, [load])

  const updateProfile = useCallback(async (request: UpdateMyProfileRequest) => {
    try {
      if (!mountedRef.current) return null
      setError(null)
      setSaving(true)
      const updated = await updateMyProfile(request)
      if (!mountedRef.current) return null
      setProfile(updated)
      return updated
    } catch (e) {
      if (!mountedRef.current) throw e
      const message = e instanceof Error ? e.message : String(e)
      setError(message)
      throw e
    } finally {
      if (mountedRef.current) setSaving(false)
    }
  }, [])

  return { profile, loading, saving, error, refetch, updateProfile }
}
