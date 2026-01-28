import { useCallback, useEffect, useRef, useState } from 'react'
import { fetchMyProfile, updateMyProfile } from '../api/profile'
import {
  commitMyProfileImage,
  deleteMyProfileImage,
  presignMyProfileImageUpload,
  putFileToPresignedUrl,
} from '../api/profileImage'
import type { MyProfile, UpdateMyProfileRequest } from '../types'

export function useMyProfile() {
  const [profile, setProfile] = useState<MyProfile | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [uploadingImage, setUploadingImage] = useState(false)
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

  const uploadProfileImage = useCallback(async (file: File) => {
    try {
      if (!mountedRef.current) return null
      setError(null)
      setUploadingImage(true)

      if (!file || file.size <= 0) throw new Error('업로드할 파일이 올바르지 않습니다.')
      if (!file.type || !file.type.startsWith('image/')) {
        throw new Error('이미지 파일만 업로드할 수 있습니다.')
      }

      const presigned = await presignMyProfileImageUpload(file)
      await putFileToPresignedUrl({
        uploadUrl: presigned.uploadUrl,
        file,
        contentType: presigned.contentType,
      })

      const updated = await commitMyProfileImage(presigned.key)
      if (!mountedRef.current) return null
      setProfile(updated)
      return updated
    } catch (e) {
      if (!mountedRef.current) throw e
      const message = e instanceof Error ? e.message : String(e)
      setError(message)
      throw e
    } finally {
      if (mountedRef.current) setUploadingImage(false)
    }
  }, [])

  const removeProfileImage = useCallback(async () => {
    try {
      if (!mountedRef.current) return null
      setError(null)
      setUploadingImage(true)
      const updated = await deleteMyProfileImage()
      if (!mountedRef.current) return null
      setProfile(updated)
      return updated
    } catch (e) {
      if (!mountedRef.current) throw e
      const message = e instanceof Error ? e.message : String(e)
      setError(message)
      throw e
    } finally {
      if (mountedRef.current) setUploadingImage(false)
    }
  }, [])

  return {
    profile,
    loading,
    saving,
    uploadingImage,
    error,
    refetch,
    updateProfile,
    uploadProfileImage,
    removeProfileImage,
  }
}
