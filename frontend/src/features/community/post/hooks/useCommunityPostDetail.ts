import { useCallback, useEffect, useState } from 'react'
import { fetchCommunityPostDetail } from '../api/posts'
import type { CommunityPostDetail } from '../types'

export function useCommunityPostDetail(postId: number | null) {
  const [post, setPost] = useState<CommunityPostDetail | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const refresh = useCallback(
    async (signal?: AbortSignal) => {
      if (postId == null || !Number.isFinite(postId)) {
        setPost(null)
        setError(null)
        setLoading(false)
        return
      }

      setError(null)
      setLoading(true)
      try {
        const json = await fetchCommunityPostDetail(postId)
        if (signal?.aborted) return
        setPost(json)
      } catch (e) {
        if (!signal?.aborted) setError(e instanceof Error ? e.message : String(e))
      } finally {
        if (!signal?.aborted) setLoading(false)
      }
    },
    [postId],
  )

  useEffect(() => {
    const controller = new AbortController()
    void refresh(controller.signal)
    return () => controller.abort()
  }, [refresh])

  return { post, error, loading, refresh }
}

