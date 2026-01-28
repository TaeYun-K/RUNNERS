import { Eye, Heart, MessageCircle } from 'lucide-react'
import { COMMUNITY_BOARD_LABEL } from '../constants'
import type { CommunityPostSummary } from '../types'
import { formatRelativeTime } from '../utils'

export function CommunityPostCard(props: { post: CommunityPostSummary }) {
  const { post } = props

  return (
    <article className="group overflow-hidden rounded-2xl border border-border bg-background transition hover:border-blue-500/30 hover:shadow-lg hover:shadow-blue-500/5">
      <div className="grid gap-4 p-5 sm:grid-cols-[1fr_140px] sm:items-start">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
            <span className="rounded-full bg-secondary px-2 py-1 text-foreground">
              {COMMUNITY_BOARD_LABEL[post.boardType] ?? post.boardType}
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
  )
}

