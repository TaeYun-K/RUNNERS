'use client';

import { useState } from 'react'
import { RefreshCw, Plus } from 'lucide-react'
import { Link } from 'react-router-dom'
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
    <section className="mx-auto max-w-3xl">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">
            커뮤니티
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">
            러너들과 이야기를 나눠보세요
          </p>
        </div>

        <Link
          to="/community/write"
          className="inline-flex h-10 items-center justify-center gap-2 rounded-full bg-primary px-4 text-sm font-semibold text-primary-foreground shadow-sm transition-colors hover:bg-primary/90"
        >
          <Plus className="h-4 w-4" />
          글쓰기
        </Link>
      </div>

      <div className="mt-6 flex flex-wrap items-center gap-2">
        {(['ALL', 'FREE', 'QNA', 'INFO'] as const).map((t) => (
          <button
            key={t}
            type="button"
            onClick={() => setBoardType(t)}
            className={[
              'h-9 rounded-full px-4 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background',
              t === boardType
                ? 'bg-primary/10 text-primary ring-1 ring-primary/20 hover:bg-primary/15'
                : 'bg-muted text-foreground hover:bg-muted/80',
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
          className="ml-auto inline-flex h-9 items-center gap-2 rounded-full border border-border/70 bg-background/50 px-3 text-sm font-medium text-foreground transition-colors hover:bg-muted disabled:opacity-60"
          disabled={loading}
          aria-label="새로고침"
        >
          <RefreshCw className={loading ? 'h-4 w-4 animate-spin' : 'h-4 w-4'} />
        </button>
      </div>

      {error ? (
        <div className="mt-6 rounded-lg border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive">
          {error}
        </div>
      ) : null}

      {loading ? (
        <div className="mt-6">
          <CommunityPostSkeletonList count={6} />
        </div>
      ) : posts.length === 0 ? (
        <div className="mt-6 rounded-2xl border border-dashed border-border/70 bg-card/60 px-6 py-16 text-center backdrop-blur">
          <p className="text-base font-semibold text-foreground">
            아직 게시글이 없어요
          </p>
          <p className="mt-2 text-sm text-muted-foreground">
            첫 글을 작성해서 커뮤니티를 시작해보세요.
          </p>
        </div>
      ) : (
        <div className="mt-6 grid gap-3">
          {posts.map((post) => (
            <CommunityPostCard key={post.postId} post={post} />
          ))}
        </div>
      )}

      <div className="mt-8 flex justify-center">
        <button
          type="button"
          onClick={() => void loadMore()}
          disabled={!nextCursor || loading || loadingMore}
          className="h-10 rounded-full border border-border/70 bg-background/50 px-6 text-sm font-medium text-foreground transition-colors hover:bg-muted disabled:cursor-not-allowed disabled:opacity-60"
        >
          {loadingMore ? '불러오는 중...' : nextCursor ? '더 보기' : '마지막입니다'}
        </button>
      </div>
    </section>
  )
}
