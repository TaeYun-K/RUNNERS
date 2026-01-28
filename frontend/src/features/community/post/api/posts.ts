import { apiFetch } from '../../../../shared/api/apiFetch'
import type {
  CommunityPostBoardType,
  CommunityPostCursorListResponse,
  CommunityPostDetail,
} from '../types'

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

export async function fetchCommunityPostDetail(postId: number) {
  const res = await apiFetch(`/api/community/posts/${postId}`)
  return (await res.json()) as CommunityPostDetail
}

