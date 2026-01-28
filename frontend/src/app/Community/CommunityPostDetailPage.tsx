import { useMemo } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, Eye, Heart, MessageCircle, RefreshCw } from 'lucide-react'
import {
  COMMUNITY_BOARD_LABEL,
  formatRelativeTime,
  useCommunityPostDetail,
} from '../../features/community'
import { NotFoundPage } from '../Error/NotFoundPage'

export function CommunityPostDetailPage() {
  const params = useParams()
  const postId = useMemo(() => {
    const raw = params.postId ?? ''
    const parsed = Number.parseInt(raw, 10)
    return Number.isFinite(parsed) ? parsed : null
  }, [params.postId])

  const { post, error, loading, refresh } = useCommunityPostDetail(postId)

  if (postId == null) return <NotFoundPage />

  return (
    <section className="mx-auto max-w-5xl rounded-2xl border border-border bg-card p-6 shadow-sm">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-3">
          <Link
            to="/community"
            className="inline-flex h-9 items-center gap-2 rounded-full border border-border bg-background px-4 text-sm font-medium text-foreground transition hover:bg-secondary/60"
          >
            <ArrowLeft className="h-4 w-4" />
            목록
          </Link>
          <h2 className="text-xl font-bold tracking-tight text-foreground">
            게시글 상세
          </h2>
        </div>

        <button
          type="button"
          onClick={() => void refresh()}
          className="inline-flex h-9 items-center gap-2 rounded-full border border-border bg-background px-4 text-sm font-medium text-foreground transition hover:bg-secondary/60 disabled:opacity-60"
          disabled={loading}
          aria-label="새로고침"
        >
          <RefreshCw className={loading ? 'h-4 w-4 animate-spin' : 'h-4 w-4'} />
          새로고침
        </button>
      </div>

      {error ? (
        <div className="mt-4 rounded-xl border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {error}
        </div>
      ) : null}

      {loading && !post ? (
        <div className="mt-6 grid gap-3 rounded-2xl border border-border bg-background p-5">
          <div className="h-4 w-24 animate-pulse rounded bg-secondary" />
          <div className="h-7 w-2/3 animate-pulse rounded bg-secondary" />
          <div className="h-4 w-full animate-pulse rounded bg-secondary" />
          <div className="h-4 w-5/6 animate-pulse rounded bg-secondary" />
        </div>
      ) : post ? (
        <article className="mt-6 overflow-hidden rounded-2xl border border-border bg-background">
          <header className="border-b border-border p-5">
            <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
              <span className="rounded-full bg-secondary px-2 py-1 text-foreground">
                {COMMUNITY_BOARD_LABEL[post.boardType] ?? post.boardType}
              </span>
              <span>{formatRelativeTime(post.createdAt)}</span>
              {post.updatedAt && post.updatedAt !== post.createdAt ? (
                <span className="text-muted-foreground/70">
                  (수정 {formatRelativeTime(post.updatedAt)})
                </span>
              ) : null}
            </div>

            <h3 className="mt-3 text-2xl font-bold tracking-tight text-foreground">
              {post.title}
            </h3>

            <div className="mt-4 flex flex-wrap items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center overflow-hidden rounded-full bg-secondary text-sm font-bold text-muted-foreground">
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
          </header>

          {post.imageUrls?.length ? (
            <div className="grid grid-cols-2 gap-3 border-b border-border p-5 sm:grid-cols-3">
              {post.imageUrls.map((url) => (
                <a
                  key={url}
                  href={url}
                  target="_blank"
                  rel="noreferrer"
                  className="group relative aspect-[4/3] overflow-hidden rounded-xl bg-secondary"
                >
                  <img
                    src={url}
                    alt=""
                    className="h-full w-full object-cover transition-transform group-hover:scale-105"
                    loading="lazy"
                    referrerPolicy="no-referrer"
                  />
                </a>
              ))}
            </div>
          ) : null}

          <div className="p-5">
            <p className="whitespace-pre-wrap text-sm leading-relaxed text-foreground">
              {post.content}
            </p>
          </div>
        </article>
      ) : null}
    </section>
  )
}

