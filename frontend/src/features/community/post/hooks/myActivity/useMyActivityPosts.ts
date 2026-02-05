import { useCallback, useEffect, useState } from 'react'
import { fetchMyPosts, fetchPostsCommented } from '../../api/posts'
import type { CommunityPostSummary } from '../../types'

/**
 * 내 활동(내가 쓴 글 / 댓글 단 글) 공통 리스트 훅.
 * mode에 따라 적절한 API(fetchMyPosts, fetchPostsCommented)를 호출하고
 * 커서 기반 페이지네이션을 동일한 형태로 제공한다.
 */
export type MyActivityMode = 'me' | 'commented'

function fetchByMode(
  mode: MyActivityMode,
  params: { cursor: string | null; size: number },
) {
  if (mode === 'me') return fetchMyPosts(params)
  return fetchPostsCommented(params)
}

/**
 * 내 활동 게시글(내가 쓴 글 / 댓글 단 글)을 불러오는 훅.
 * 뷰 레이어에서는 보통 mode만 바꿔서 이 훅 하나를 재사용한다.
 */
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

