import { useCallback, useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { Heart, MessageCircle } from 'lucide-react'
import { useAuth } from '../../../auth'
import type { CommunityComment } from '../types'
import { formatRelativeTime } from '../../shared/formatRelativeTime'
import {
  fetchCommunityCommentRecommendStatus,
  recommendCommunityComment,
  unrecommendCommunityComment,
} from '../api/comments'
import type { CommunityCommentRecommendResponse } from '../types'

export function CommunityCommentItem(props: {
  comment: CommunityComment
  onReply?: (commentId: number) => void
}) {
  const { comment, onReply } = props
  const isReply = comment.parentId != null
  const navigate = useNavigate()
  const location = useLocation()
  const { accessToken } = useAuth()

  const [recommend, setRecommend] =
    useState<CommunityCommentRecommendResponse | null>(null)
  const [recommendLoading, setRecommendLoading] = useState(false)
  const [recommendError, setRecommendError] = useState<string | null>(null)

  const refreshRecommend = useCallback(
    async (signal?: AbortSignal) => {
      if (!accessToken) {
        setRecommend(null)
        setRecommendError(null)
        setRecommendLoading(false)
        return
      }

      setRecommendError(null)
      setRecommendLoading(true)
      try {
        const json = await fetchCommunityCommentRecommendStatus(
          comment.postId,
          comment.commentId,
          { signal },
        )
        if (signal?.aborted) return
        setRecommend(json)
      } catch (e) {
        if (!signal?.aborted) {
          setRecommendError(e instanceof Error ? e.message : String(e))
        }
      } finally {
        if (!signal?.aborted) setRecommendLoading(false)
      }
    },
    [accessToken, comment.commentId, comment.postId],
  )

  useEffect(() => {
    const controller = new AbortController()
    void refreshRecommend(controller.signal)
    return () => controller.abort()
  }, [refreshRecommend])

  const recommendCount =
    recommend?.recommendCount ?? (comment.recommendCount ?? 0)
  const isRecommended = recommend?.recommended ?? false

  const handleToggleRecommend = async () => {
    if (!accessToken) {
      navigate('/login', {
        replace: false,
        state: { from: `${location.pathname}${location.search}${location.hash}` },
      })
      return
    }

    setRecommendError(null)
    setRecommendLoading(true)
    try {
      const json = isRecommended
        ? await unrecommendCommunityComment(comment.postId, comment.commentId)
        : await recommendCommunityComment(comment.postId, comment.commentId)
      setRecommend(json)
    } catch (e) {
      setRecommendError(e instanceof Error ? e.message : String(e))
    } finally {
      setRecommendLoading(false)
    }
  }

  return (
    <div
      className={[
        'rounded-xl border border-border bg-background p-4',
        isReply ? 'ml-6' : '',
      ].join(' ')}
    >
      <div className="flex items-start justify-between gap-3">
        <Link to={`/users/${comment.authorId}`} className="flex items-center gap-2">
          <div className="flex h-8 w-8 items-center justify-center overflow-hidden rounded-full bg-secondary text-xs font-bold text-muted-foreground">
            {comment.authorPicture ? (
              <img
                src={comment.authorPicture}
                alt=""
                className="h-full w-full object-cover"
                loading="lazy"
                referrerPolicy="no-referrer"
              />
            ) : (
              <span>{comment.authorName?.charAt(0) ?? '?'}</span>
            )}
          </div>
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold text-foreground">
              {comment.authorName}
            </p>
            <p className="text-xs text-muted-foreground">
              {formatRelativeTime(comment.createdAt)}
            </p>
          </div>
        </Link>

        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={handleToggleRecommend}
            disabled={recommendLoading}
            aria-pressed={isRecommended}
            aria-label={isRecommended ? '댓글 추천 취소' : '댓글 추천'}
            className={
              isRecommended
                ? 'inline-flex h-8 items-center gap-1.5 rounded-full bg-secondary/40 px-3 text-xs font-semibold text-rose-600 transition hover:bg-secondary/60 disabled:cursor-not-allowed disabled:opacity-60'
                : 'inline-flex h-8 items-center gap-1.5 rounded-full border border-border bg-background px-3 text-xs font-semibold text-foreground transition hover:bg-secondary/60 disabled:cursor-not-allowed disabled:opacity-60'
            }
          >
            <Heart
              className={
                isRecommended
                  ? 'h-3.5 w-3.5 fill-rose-600 text-rose-600'
                  : 'h-3.5 w-3.5'
              }
            />
            {recommendCount}
          </button>

          {onReply ? (
            <button
              type="button"
              onClick={() => onReply(comment.commentId)}
              className="inline-flex h-8 items-center gap-1.5 rounded-full border border-border bg-background px-3 text-xs font-semibold text-foreground transition hover:bg-secondary/60"
            >
              <MessageCircle className="h-3.5 w-3.5" />
              답글
            </button>
          ) : null}
        </div>
      </div>

      <p className="mt-3 whitespace-pre-wrap text-sm leading-relaxed text-foreground">
        {comment.content}
      </p>

      {recommendError ? (
        <div className="mt-3 rounded-xl border border-destructive/20 bg-destructive/10 px-3 py-2 text-xs text-destructive">
          {recommendError}
        </div>
      ) : null}
    </div>
  )
}
