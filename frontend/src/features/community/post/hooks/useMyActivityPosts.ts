import { useCallback, useEffect, useState } from 'react'
import { fetchMyPosts, fetchPostsCommented } from '../api/posts'
import type { CommunityPostSummary } from '../types'

export type MyActivityMode = 'me' | 'commented'

function fetchByMode(
  mode: MyActivityMode,
  params: { cursor: string | null; size: number },
) {
  if (mode === 'me') return fetchMyPosts(params)
  return fetchPostsCommented(params)
}

export function useMyActivityPosts(
  mode: MyActivityMode,
  params: { size?: number } = {},
) {
  const [posts, setPosts] = useState<CommunityPostSummary[]>([])
  const [nextCursor, setNextCursor] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)
  const size = params.size ?? 20

  const loadInitial = useCallback(
    async (signal?: AbortSignal) => {
      setError(null)
      setLoading(true)
      try {
        const json = await fetchByMode(mode, { cursor: null, size })
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
    [mode, size],
  )

  const loadMore = useCallback(async () => {
    if (!nextCursor || loading || loadingMore) return
    setError(null)
    setLoadingMore(true)
    try {
      const json = await fetchByMode(mode, { cursor: nextCursor, size })
      setPosts((prev) => [...prev, ...(json.posts ?? [])])
      setNextCursor(json.nextCursor ?? null)
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
    } finally {
      setLoadingMore(false)
    }
  }, [loading, loadingMore, mode, nextCursor, size])

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
