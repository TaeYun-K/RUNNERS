import { useCallback, useEffect, useState } from 'react'
import { Eye, Heart, MessageCircle, RefreshCw } from 'lucide-react'
import {
  type CommunityPostBoardType,
  type CommunityPostSummary,
  fetchCommunityPosts,
} from './communityApi'

const BOARD_LABEL: Record<CommunityPostBoardType, string> = {
  FREE: '자유',
  QNA: '질문',
  INFO: '정보',
}

function formatRelativeTime(iso: string) {
  const created = new Date(iso)
  const diffMs = Date.now() - created.getTime()
  if (!Number.isFinite(diffMs)) return ''

  const seconds = Math.floor(diffMs / 1000)
  if (seconds < 60) return '방금 전'

  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return `${minutes}분 전`

  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}시간 전`

  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}일 전`

  return created.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

export function CommunityPage() {
  const [boardType, setBoardType] = useState<CommunityPostBoardType | 'ALL'>(
    'ALL',
  )
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
          boardType: boardType === 'ALL' ? null : boardType,
          cursor: null,
          size: 20,
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
    [boardType],
  )

  const loadMore = useCallback(async () => {
    if (!nextCursor || loading || loadingMore) return
    setError(null)
    setLoadingMore(true)
    try {
      const json = await fetchCommunityPosts({
        boardType: boardType === 'ALL' ? null : boardType,
        cursor: nextCursor,
        size: 20,
      })
      setPosts((prev) => [...prev, ...(json.posts ?? [])])
      setNextCursor(json.nextCursor ?? null)
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
    } finally {
      setLoadingMore(false)
    }
  }, [boardType, loading, loadingMore, nextCursor])

  useEffect(() => {
    const controller = new AbortController()
    void loadInitial(controller.signal)
    return () => controller.abort()
  }, [loadInitial])

  return (
    <section className="mx-auto max-w-5xl rounded-2xl border border-border bg-card p-6 shadow-sm">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 className="text-xl font-bold tracking-tight text-foreground">
            커뮤니티
          </h2>
          <p className="mt-2 text-sm text-muted-foreground">
            `GET /api/community/posts` 조회 결과를 목록 UI로 렌더링합니다.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {(['ALL', 'FREE', 'QNA', 'INFO'] as const).map((t) => (
            <button
              key={t}
              type="button"
              onClick={() => setBoardType(t)}
              className={[
                'h-9 rounded-full border px-4 text-sm font-medium transition',
                t === boardType
                  ? 'border-blue-500/40 bg-blue-600/10 text-blue-600'
                  : 'border-border bg-background text-foreground hover:bg-secondary/60',
              ].join(' ')}
              disabled={loading}
            >
              {t === 'ALL'
                ? '전체'
                : BOARD_LABEL[t as CommunityPostBoardType] ?? t}
            </button>
          ))}

          <button
            type="button"
            onClick={() => void loadInitial()}
            className="inline-flex h-9 items-center gap-2 rounded-full border border-border bg-background px-4 text-sm font-medium text-foreground transition hover:bg-secondary/60 disabled:opacity-60"
            disabled={loading}
            aria-label="새로고침"
          >
            <RefreshCw className={loading ? 'h-4 w-4 animate-spin' : 'h-4 w-4'} />
            새로고침
          </button>
        </div>
      </div>

      {error ? (
        <div className="mt-4 rounded-xl border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {error}
        </div>
      ) : null}

      {loading ? (
        <div className="mt-6 grid gap-4">
          {Array.from({ length: 6 }).map((_, i) => (
            <div
              key={`skeleton-${i}`}
              className="grid gap-3 rounded-2xl border border-border bg-background p-5"
            >
              <div className="h-4 w-24 animate-pulse rounded bg-secondary" />
              <div className="h-5 w-2/3 animate-pulse rounded bg-secondary" />
              <div className="h-4 w-full animate-pulse rounded bg-secondary" />
              <div className="h-4 w-1/2 animate-pulse rounded bg-secondary" />
            </div>
          ))}
        </div>
      ) : posts.length === 0 ? (
        <div className="mt-6 rounded-2xl border border-dashed border-border bg-background px-6 py-16 text-center">
          <p className="text-base font-semibold text-foreground">
            아직 게시글이 없어요
          </p>
          <p className="mt-2 text-sm text-muted-foreground">
            첫 글을 작성해서 커뮤니티를 시작해보세요.
          </p>
        </div>
      ) : (
        <div className="mt-6 grid gap-4">
          {posts.map((post) => (
            <article
              key={post.postId}
              className="group overflow-hidden rounded-2xl border border-border bg-background transition hover:border-blue-500/30 hover:shadow-lg hover:shadow-blue-500/5"
            >
              <div className="grid gap-4 p-5 sm:grid-cols-[1fr_140px] sm:items-start">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                    <span className="rounded-full bg-secondary px-2 py-1 text-foreground">
                      {BOARD_LABEL[post.boardType] ?? post.boardType}
                    </span>
                    <span>{formatRelativeTime(post.createdAt)}</span>
                  </div>

                  <h3 className="mt-2 line-clamp-1 text-lg font-bold text-foreground transition-colors group-hover:text-blue-600">
                    {post.title}
                  </h3>

                  <p className="mt-2 line-clamp-2 text-sm text-muted-foreground">
                    {post.contentPreview}
                  </p>

                  <div className="mt-4 flex flex-wrap items-center justify-between gap-4">
                    <div className="flex items-center gap-3">
                      <div className="flex h-9 w-9 items-center justify-center overflow-hidden rounded-full bg-secondary text-sm font-bold text-muted-foreground">
                        {post.authorPicture ? (
                          <img
                            src={post.authorPicture}
                            alt=""
                            className="h-full w-full object-cover"
                            loading="lazy"
                            referrerPolicy="no-referrer"
                          />
                        ) : (
                          <span>{post.authorName?.charAt(0) ?? '?'}</span>
                        )}
                      </div>
                      <div className="min-w-0">
                        <p className="truncate text-sm font-medium text-foreground">
                          {post.authorName}
                        </p>
                        {typeof post.authorTotalDistanceKm === 'number' ? (
                          <p className="text-xs text-muted-foreground">
                            누적 {post.authorTotalDistanceKm.toFixed(1)} km
                          </p>
                        ) : null}
                      </div>
                    </div>

                    <div className="flex items-center gap-4 text-sm text-muted-foreground">
                      <span className="inline-flex items-center gap-1.5">
                        <Heart className="h-4 w-4" />
                        {post.recommendCount}
                      </span>
                      <span className="inline-flex items-center gap-1.5">
                        <MessageCircle className="h-4 w-4" />
                        {post.commentCount}
                      </span>
                      <span className="inline-flex items-center gap-1.5">
                        <Eye className="h-4 w-4" />
                        {post.viewCount}
                      </span>
                    </div>
                  </div>
                </div>

                {post.thumbnailUrl ? (
                  <div className="relative hidden aspect-[4/3] overflow-hidden rounded-xl bg-secondary sm:block">
                    <img
                      src={post.thumbnailUrl}
                      alt=""
                      className="h-full w-full object-cover"
                      loading="lazy"
                      referrerPolicy="no-referrer"
                    />
                  </div>
                ) : null}
              </div>
            </article>
          ))}
        </div>
      )}

      <div className="mt-6 flex justify-center">
        <button
          type="button"
          onClick={() => void loadMore()}
          disabled={!nextCursor || loading || loadingMore}
          className="h-11 rounded-xl border border-border bg-background px-6 text-sm font-semibold text-foreground transition hover:bg-secondary/60 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {loadingMore ? '불러오는 중…' : nextCursor ? '더 보기' : '마지막입니다'}
        </button>
      </div>
    </section>
  )
}
