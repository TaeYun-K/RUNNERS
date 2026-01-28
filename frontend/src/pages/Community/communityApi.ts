import { apiFetch } from '../../utils/api/apiFetch'

export type CommunityPostBoardType = 'FREE' | 'QNA' | 'INFO'

export type CommunityPostSummary = {
  postId: number
  authorId: number
  authorName: string
  authorPicture: string | null
  authorTotalDistanceKm: number | null
  boardType: CommunityPostBoardType
  title: string
  contentPreview: string
  thumbnailUrl: string | null
  viewCount: number
  recommendCount: number
  commentCount: number
  createdAt: string
}

export type CommunityPostCursorListResponse = {
  posts: CommunityPostSummary[]
  nextCursor: string | null
}

export async function fetchCommunityPosts(params: {
  boardType?: CommunityPostBoardType | null
  cursor?: string | null
  size?: number
}) {
  const search = new URLSearchParams()
  if (params.boardType) search.set('boardType', params.boardType)
  if (params.cursor) search.set('cursor', params.cursor)
  search.set('size', String(params.size ?? 20))

  const res = await apiFetch(`/api/community/posts?${search.toString()}`)
  return (await res.json()) as CommunityPostCursorListResponse
}

