import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  fetchCommunityPostRecommendStatus,
  recommendCommunityPost,
  unrecommendCommunityPost,
} from '../../api/posts'
import type { CommunityPostRecommendResponse } from '../../types'

export function useCommunityPostRecommend(postId: number | null, enabled: boolean) {
  const [recommend, setRecommend] = useState<CommunityPostRecommendResponse | null>(
    null,
  )
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const refresh = useCallback(
    async (signal?: AbortSignal) => {
      if (!enabled || postId == null || !Number.isFinite(postId)) {
        setRecommend(null)
        setError(null)
        setLoading(false)
        return
      }

      setError(null)
      setLoading(true)
      try {
        const json = await fetchCommunityPostRecommendStatus(postId, { signal })
        if (signal?.aborted) return
        setRecommend(json)
      } catch (e) {
        if (!signal?.aborted) setError(e instanceof Error ? e.message : String(e))
      } finally {
        if (!signal?.aborted) setLoading(false)
      }
    },
    [enabled, postId],
  )

  useEffect(() => {
    const controller = new AbortController()
    void refresh(controller.signal)
    return () => controller.abort()
  }, [refresh])

  const toggle = useCallback(async () => {
    if (!enabled) throw new Error('Login required')
    if (postId == null) throw new Error('postId is missing')

    setError(null)
    setLoading(true)
    try {
      const isRecommended = recommend?.recommended ?? false
      const json = isRecommended
        ? await unrecommendCommunityPost(postId)
        : await recommendCommunityPost(postId)
      setRecommend(json)
      return json
    } catch (e) {
      const message = e instanceof Error ? e.message : String(e)
      setError(message)
      throw e
    } finally {
      setLoading(false)
    }
  }, [enabled, postId, recommend?.recommended])

  const isRecommended = useMemo(() => recommend?.recommended ?? false, [recommend])
  const recommendCount = useMemo(
    () => (recommend?.recommendCount ?? 0),
    [recommend],
  )

  return { recommend, isRecommended, recommendCount, error, loading, refresh, toggle }
}

