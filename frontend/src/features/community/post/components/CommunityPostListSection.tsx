'use client';

import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Plus, Edit3, MessageCircle, ArrowLeft, RefreshCw } from 'lucide-react'
import { useAuth } from '../../../auth'
import { COMMUNITY_BOARD_LABEL } from '../constants'
import type {
  CommunityPostBoardType,
  CommunityPostSummary,
} from '../types'
import {
  useCommunityPosts,
  useMyActivityCounts,
  useMyActivityPosts,
  type MyActivityMode,
} from '..'
import { CommunityPostCard } from './CommunityPostCard'
import { CommunityPostSkeletonList } from './CommunityPostSkeletonList'

const ACTIVITY_VIEW_CONFIG: Record<
  MyActivityMode,
  { title: string; emptyTitle: string; emptyDescription: string }
> = {
  me: {
    title: '내가 쓴 글',
    emptyTitle: '작성한 글이 없어요',
    emptyDescription: '커뮤니티에서 첫 글을 작성해보세요.',
  },
  commented: {
    title: '댓글 단 글',
    emptyTitle: '댓글 단 글이 없어요',
    emptyDescription: '커뮤니티에서 글에 댓글을 남겨보세요.',
  },
}

type PostListBodyProps = {
  error: string | null
  loading: boolean
  loadingMore: boolean
  posts: CommunityPostSummary[]
  nextCursor: string | null
  loadMore: () => void
  emptyTitle: string
  emptyDescription: string
  emptyAction?: React.ReactNode
}

/** 에러 / 로딩 / 빈 목록 / 리스트 / 더보기 버튼 공통 UI */
function PostListBody({
  error,
  loading,
  loadingMore,
  posts,
  nextCursor,
  loadMore,
  emptyTitle,
  emptyDescription,
  emptyAction,
}: PostListBodyProps) {
  return (
    <>
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
          <p className="text-base font-semibold text-foreground">{emptyTitle}</p>
          <p className="mt-2 text-sm text-muted-foreground">
            {emptyDescription}
          </p>
          {emptyAction}
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
          onClick={() => loadMore()}
          disabled={!nextCursor || loading || loadingMore}
          className="h-10 rounded-full border border-border/70 bg-background/50 px-6 text-sm font-medium text-foreground transition-colors hover:bg-muted disabled:cursor-not-allowed disabled:opacity-60"
        >
          {loadingMore ? '불러오는 중...' : nextCursor ? '더 보기' : '마지막입니다'}
        </button>
      </div>
    </>
  )
}

/** 전체 목록: 보드 필터 + 내 활동 카드 + useCommunityPosts */
export function CommunityMainSection() {
  const { accessToken } = useAuth()
  const [boardType, setBoardType] = useState<CommunityPostBoardType | 'ALL'>(
    'ALL',
  )
  const { posts, nextCursor, error, loading, loadingMore, refresh, loadMore } =
    useCommunityPosts({
      boardType: boardType === 'ALL' ? null : boardType,
      size: 20,
    })
  const {
    myPostsCount,
    commentedCount,
    loading: countsLoading,
  } = useMyActivityCounts({ enabled: !!accessToken })

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

      {accessToken ? (
        <div className="mt-6 grid grid-cols-2 gap-3">
          <Link
            to="/community/me"
            className="flex items-center gap-3 rounded-xl border border-border/70 bg-card/60 px-4 py-3 transition-colors hover:bg-muted/50"
          >
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10 text-primary">
              <Edit3 className="h-5 w-5" />
            </div>
            <div>
              <p className="text-sm font-medium text-foreground">내가 쓴 글</p>
              <p className="text-lg font-bold text-foreground">
                {countsLoading ? '-' : myPostsCount ?? 0}
                <span className="ml-0.5 text-sm font-normal text-muted-foreground">
                  건
                </span>
              </p>
            </div>
          </Link>
          <Link
            to="/community/commented"
            className="flex items-center gap-3 rounded-xl border border-border/70 bg-card/60 px-4 py-3 transition-colors hover:bg-muted/50"
          >
            <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-muted text-foreground">
              <MessageCircle className="h-5 w-5" />
            </div>
            <div>
              <p className="text-sm font-medium text-foreground">댓글 단 글</p>
              <p className="text-lg font-bold text-foreground">
                {countsLoading ? '-' : commentedCount ?? 0}
                <span className="ml-0.5 text-sm font-normal text-muted-foreground">
                  건
                </span>
              </p>
            </div>
          </Link>
        </div>
      ) : null}

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
          <RefreshCw
            className={loading ? 'h-4 w-4 animate-spin' : 'h-4 w-4'}
          />
        </button>
      </div>

      <PostListBody
        error={error}
        loading={loading}
        loadingMore={loadingMore}
        posts={posts}
        nextCursor={nextCursor}
        loadMore={loadMore}
        emptyTitle="아직 게시글이 없어요"
        emptyDescription="첫 글을 작성해서 커뮤니티를 시작해보세요."
      />
    </section>
  )
}

/** 내 글 / 댓글 단 글: 뒤로가기 + 제목 + useMyActivityPosts */
export function CommunityActivitySection({ mode }: { mode: MyActivityMode }) {
  const config = ACTIVITY_VIEW_CONFIG[mode]
  const {
    posts,
    nextCursor,
    error,
    loading,
    loadingMore,
    loadMore,
  } = useMyActivityPosts(mode, { size: 20 })

  return (
    <section className="mx-auto max-w-3xl">
      <div className="flex items-center gap-3">
        <Link
          to="/community"
          className="inline-flex h-9 w-9 items-center justify-center rounded-full text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
          aria-label="커뮤니티로 돌아가기"
        >
          <ArrowLeft className="h-5 w-5" />
        </Link>
        <h1 className="text-2xl font-bold tracking-tight text-foreground">
          {config.title}
        </h1>
      </div>

      <PostListBody
        error={error}
        loading={loading}
        loadingMore={loadingMore}
        posts={posts}
        nextCursor={nextCursor}
        loadMore={loadMore}
        emptyTitle={config.emptyTitle}
        emptyDescription={config.emptyDescription}
        emptyAction={
          <Link
            to="/community"
            className="mt-4 inline-block text-sm font-medium text-primary hover:underline"
          >
            커뮤니티로 가기
          </Link>
        }
      />
    </section>
  )
}

