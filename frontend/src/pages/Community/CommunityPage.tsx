import { useState } from 'react'
import { RefreshCw } from 'lucide-react'
import {
  COMMUNITY_BOARD_LABEL,
  type CommunityPostBoardType,
  useCommunityPosts,
} from '../../features/community/post'
import { CommunityPostCard } from '../../features/community/post/components/CommunityPostCard'
import { CommunityPostSkeletonList } from '../../features/community/post/components/CommunityPostSkeletonList'

export function CommunityPage() {
  const [boardType, setBoardType] = useState<CommunityPostBoardType | 'ALL'>(
    'ALL',
  )
  const { posts, nextCursor, error, loading, loadingMore, refresh, loadMore } =
    useCommunityPosts({
      boardType: boardType === 'ALL' ? null : boardType,
      size: 20,
    })

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
                : COMMUNITY_BOARD_LABEL[t as CommunityPostBoardType] ?? t}
            </button>
          ))}

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
      </div>

      {error ? (
        <div className="mt-4 rounded-xl border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {error}
        </div>
      ) : null}

      {loading ? (
        <div className="mt-6">
          <CommunityPostSkeletonList count={6} />
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
            <CommunityPostCard key={post.postId} post={post} />
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
