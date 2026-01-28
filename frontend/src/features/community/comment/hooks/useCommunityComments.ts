import { useCallback, useEffect, useMemo, useState } from 'react'
import { createCommunityComment, fetchCommunityComments } from '../api/comments'
import type { CommunityComment } from '../types'

export function useCommunityComments(params: {
  postId: number | null
  size?: number
}) {
  const [comments, setComments] = useState<CommunityComment[]>([])
  const [nextCursor, setNextCursor] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)
  const [creating, setCreating] = useState(false)

  const postId = params.postId
  const size = params.size ?? 20

  const refresh = useCallback(
    async (signal?: AbortSignal) => {
      if (postId == null || !Number.isFinite(postId)) {
        setComments([])
        setNextCursor(null)
        setError(null)
        setLoading(false)
        return
      }

      setError(null)
      setLoading(true)
      try {
        const json = await fetchCommunityComments({ postId, cursor: null, size })
        if (signal?.aborted) return
        setComments(json.comments ?? [])
        setNextCursor(json.nextCursor ?? null)
      } catch (e) {
        if (!signal?.aborted) setError(e instanceof Error ? e.message : String(e))
      } finally {
        if (!signal?.aborted) setLoading(false)
      }
    },
    [postId, size],
  )

  const loadMore = useCallback(async () => {
    if (postId == null || !nextCursor || loading || loadingMore) return
    setError(null)
    setLoadingMore(true)
    try {
      const json = await fetchCommunityComments({ postId, cursor: nextCursor, size })
      setComments((prev) => [...prev, ...(json.comments ?? [])])
      setNextCursor(json.nextCursor ?? null)
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
    } finally {
      setLoadingMore(false)
    }
  }, [loading, loadingMore, nextCursor, postId, size])

  const create = useCallback(
    async (body: { content: string; parentId?: number | null }) => {
      if (postId == null) throw new Error('postId is missing')
      setCreating(true)
      setError(null)
      try {
        const res = await createCommunityComment({ postId, body })
        setComments((prev) => [res.comment, ...prev])
        return res
      } finally {
        setCreating(false)
      }
    },
    [postId],
  )

  useEffect(() => {
    const controller = new AbortController()
    void refresh(controller.signal)
    return () => controller.abort()
  }, [refresh])

  const hasMore = useMemo(() => Boolean(nextCursor), [nextCursor])

  return {
    comments,
    nextCursor,
    hasMore,
    error,
    loading,
    loadingMore,
    creating,
    refresh,
    loadMore,
    create,
  }
}

