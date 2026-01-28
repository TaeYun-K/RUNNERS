import { useMemo, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, Eye, Heart, MessageCircle, RefreshCw } from 'lucide-react'
import { useAuth } from '../../features/auth'
import { useCommunityComments } from '../../features/community/comment'
import { CommunityCommentItem } from '../../features/community/comment/components/CommunityCommentItem'
import { COMMUNITY_BOARD_LABEL, useCommunityPostDetail } from '../../features/community/post'
import { formatRelativeTime } from '../../features/community/shared'
import { NotFoundPage } from '../Error/NotFoundPage'

export function CommunityPostDetailPage() {
  const params = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const { accessToken } = useAuth()
  const postId = useMemo(() => {
    const raw = params.postId ?? ''
    const parsed = Number.parseInt(raw, 10)
    return Number.isFinite(parsed) ? parsed : null
  }, [params.postId])

  const { post, error, loading, refresh } = useCommunityPostDetail(postId)
  const {
    comments,
    error: commentsError,
    loading: commentsLoading,
    loadingMore: commentsLoadingMore,
    hasMore: commentsHasMore,
    loadMore: loadMoreComments,
    creating,
    create: createComment,
  } = useCommunityComments({ postId, size: 20 })

  const [draft, setDraft] = useState('')
  const [replyTo, setReplyTo] = useState<number | null>(null)
  const [submitError, setSubmitError] = useState<string | null>(null)

  if (postId == null) return <NotFoundPage />

  const handleSubmit = async () => {
    if (!accessToken) {
      navigate('/login', {
        replace: false,
        state: { from: `${location.pathname}${location.search}${location.hash}` },
      })
      return
    }
    const content = draft.trim()
    if (!content) return

    setSubmitError(null)
    try {
      await createComment({ content, parentId: replyTo })
      setDraft('')
      setReplyTo(null)
      void refresh()
    } catch (e) {
      setSubmitError(e instanceof Error ? e.message : String(e))
    }
  }

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
              <Link
                to={`/users/${post.authorId}`}
                className="flex items-center gap-3"
              >
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
              </Link>

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

      <section className="mt-8">
        <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h3 className="text-lg font-bold text-foreground">
              댓글 {post ? post.commentCount : ''}
            </h3>
          </div>
        </div>

        <div className="mt-4 rounded-2xl border border-border bg-background p-4">
          {replyTo ? (
            <div className="mb-3 flex items-center justify-between gap-3 rounded-xl border border-border bg-secondary/40 px-3 py-2 text-sm text-foreground">
              <span>답글 작성 중 (parentId: {replyTo})</span>
              <button
                type="button"
                className="text-xs font-semibold text-muted-foreground transition hover:text-foreground"
                onClick={() => setReplyTo(null)}
              >
                취소
              </button>
            </div>
          ) : null}

          <textarea
            value={draft}
            onChange={(e) => setDraft(e.target.value)}
            placeholder={accessToken ? '댓글을 입력하세요…' : '로그인 후 댓글을 작성할 수 있어요.'}
            disabled={!accessToken || creating}
            className="min-h-24 w-full resize-y rounded-xl border border-border bg-background px-4 py-3 text-sm text-foreground outline-none transition focus:border-blue-500/40 focus:ring-2 focus:ring-blue-500/20 disabled:cursor-not-allowed disabled:opacity-60"
          />

          {submitError ? (
            <div className="mt-3 rounded-xl border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive">
              {submitError}
            </div>
          ) : null}

          <div className="mt-3 flex flex-wrap items-center justify-between gap-3">
            <p className="text-xs text-muted-foreground">
              {draft.length.toLocaleString()} / 16000
            </p>
            <div className="flex items-center gap-2">
              {!accessToken ? (
                <button
                  type="button"
                  onClick={handleSubmit}
                  className="h-9 rounded-xl bg-blue-600 px-4 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700"
                >
                  로그인하고 작성
                </button>
              ) : (
                <button
                  type="button"
                  onClick={handleSubmit}
                  disabled={creating || draft.trim().length === 0}
                  className="h-9 rounded-xl bg-blue-600 px-4 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {creating ? '등록 중…' : '댓글 등록'}
                </button>
              )}
            </div>
          </div>
        </div>

        {commentsError ? (
          <div className="mt-4 rounded-xl border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive">
            {commentsError}
          </div>
        ) : null}

        {commentsLoading ? (
          <div className="mt-4 grid gap-3">
            {Array.from({ length: 4 }).map((_, i) => (
              <div
                key={`comment-skeleton-${i}`}
                className="grid gap-3 rounded-xl border border-border bg-background p-4"
              >
                <div className="h-4 w-24 animate-pulse rounded bg-secondary" />
                <div className="h-4 w-full animate-pulse rounded bg-secondary" />
                <div className="h-4 w-3/4 animate-pulse rounded bg-secondary" />
              </div>
            ))}
          </div>
        ) : comments.length === 0 ? (
          <div className="mt-4 rounded-2xl border border-dashed border-border bg-background px-6 py-10 text-center">
            <p className="text-sm font-semibold text-foreground">
              아직 댓글이 없어요
            </p>
            <p className="mt-2 text-sm text-muted-foreground">
              첫 댓글을 남겨보세요.
            </p>
          </div>
        ) : (
          <div className="mt-4 grid gap-3">
            {comments.map((c) => (
              <CommunityCommentItem
                key={c.commentId}
                comment={c}
                onReply={(id) => setReplyTo(id)}
              />
            ))}
          </div>
        )}

        <div className="mt-4 flex justify-center">
          <button
            type="button"
            onClick={() => void loadMoreComments()}
            disabled={!commentsHasMore || commentsLoading || commentsLoadingMore}
            className="h-11 rounded-xl border border-border bg-background px-6 text-sm font-semibold text-foreground transition hover:bg-secondary/60 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {commentsLoadingMore
              ? '불러오는 중…'
              : commentsHasMore
                ? '댓글 더 보기'
                : '마지막입니다'}
          </button>
        </div>
      </section>
    </section>
  )
}
