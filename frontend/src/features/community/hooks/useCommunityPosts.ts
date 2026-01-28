import { useCallback, useEffect, useState } from 'react'
import { fetchCommunityPosts } from '../api'
import type { CommunityPostBoardType, CommunityPostSummary } from '../types'

export function useCommunityPosts(params: {
  boardType?: CommunityPostBoardType | null
  size?: number
}) {
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
        const json = await fetchCommunityPosts({
          boardType: params.boardType ?? null,
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
    [params.boardType, params.size],
  )

  const loadMore = useCallback(async () => {
    if (!nextCursor || loading || loadingMore) return
    setError(null)
    setLoadingMore(true)
    try {
      const json = await fetchCommunityPosts({
        boardType: params.boardType ?? null,
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
  }, [loading, loadingMore, nextCursor, params.boardType, params.size])

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

