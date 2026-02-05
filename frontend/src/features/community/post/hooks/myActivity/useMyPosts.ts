import { useCallback, useEffect, useState } from 'react'
import { fetchMyPosts } from '../../api/posts'
import type { CommunityPostSummary } from '../../types'

/**
 * 내 활동 중 \"내가 쓴 글\"만 별도로 조회하고 싶을 때 사용하는 훅.
 * 내부적으로는 fetchMyPosts만 사용하며, UI에서 별도 모드 구분이 필요할 때 쓴다.
 */
export function useMyPosts(params: { size?: number } = {}) {
  const [posts, setPosts] = useState<CommunityPostSummary[]>([])
  const [nextCursor, setNextCursor] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)

  const loadInitial = useCallback(
    async (signal?: AbortSignal) => {
      setError(null)
      setLoading(true)
      try {
        const json = await fetchMyPosts({
          cursor: null,
          size: params.size ?? 20,
        })
        if (signal?.aborted) return
        setPosts(json.posts ?? [])
        setNextCursor(json.nextCursor ?? null)
      } catch (e) {
        if (!signal?.aborted)
          setError(e instanceof Error ? e.message : String(e))
      } finally {
        if (!signal?.aborted) setLoading(false)
      }
    },
    [params.size],
  )

  const loadMore = useCallback(async () => {
    if (!nextCursor || loading || loadingMore) return
    setError(null)
    setLoadingMore(true)
    try {
      const json = await fetchMyPosts({
        cursor: nextCursor,
        size: params.size ?? 20,
      })
      setPosts((prev) => [...prev, ...(json.posts ?? [])])
      setNextCursor(json.nextCursor ?? null)
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
    } finally {
      setLoadingMore(false)
    }
  }, [loading, loadingMore, nextCursor, params.size])

  useEffect(() => {
    const controller = new AbortController()
    void loadInitial(controller.signal)
    return () => controller.abort()
  }, [loadInitial])

  return {
    posts,
    nextCursor,
    error,
    loading,
    loadingMore,
    refresh: loadInitial,
    loadMore,
  }
}

