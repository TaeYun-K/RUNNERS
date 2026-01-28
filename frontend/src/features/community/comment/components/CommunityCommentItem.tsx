import { Link } from 'react-router-dom'
import { MessageCircle } from 'lucide-react'
import type { CommunityComment } from '../types'
import { formatRelativeTime } from '../../shared/formatRelativeTime'

export function CommunityCommentItem(props: {
  comment: CommunityComment
  onReply?: (commentId: number) => void
}) {
  const { comment, onReply } = props
  const isReply = comment.parentId != null

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

      <p className="mt-3 whitespace-pre-wrap text-sm leading-relaxed text-foreground">
        {comment.content}
      </p>
    </div>
  )
}
