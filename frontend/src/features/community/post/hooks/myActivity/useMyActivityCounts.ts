import { useCallback, useEffect, useState } from 'react'
import {
  fetchMyPostsCount,
  fetchPostsCommentedCount,
} from '../../api/posts'

export function useMyActivityCounts(params?: { enabled?: boolean }) {
  const enabled = params?.enabled ?? true
  const [myPostsCount, setMyPostsCount] = useState<number | null>(null)
  const [commentedCount, setCommentedCount] = useState<number | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async (signal?: AbortSignal) => {
    setError(null)
    setLoading(true)
    try {
      const [myRes, commentedRes] = await Promise.all([
        fetchMyPostsCount(),
        fetchPostsCommentedCount(),
      ])
      if (signal?.aborted) return
      setMyPostsCount(myRes.count)
      setCommentedCount(commentedRes.count)
    } catch (e) {
      if (!signal?.aborted)
        setError(e instanceof Error ? e.message : String(e))
    } finally {
      if (!signal?.aborted) setLoading(false)
    }
  }, [])

  useEffect(() => {
    if (!enabled) return
    const controller = new AbortController()
    void load(controller.signal)
    return () => controller.abort()
  }, [enabled, load])

  return {
    myPostsCount,
    commentedCount,
    loading,
    error,
    refresh: load,
  }
}

