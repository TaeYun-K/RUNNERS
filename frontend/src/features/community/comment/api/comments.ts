import { apiFetch } from '../../../../shared/api/apiFetch'
import type {
  CommunityCommentCursorListResponse,
  CommunityCommentMutationResponse,
  CreateCommunityCommentRequest,
} from '../types'

export async function fetchCommunityComments(params: {
  postId: number
  cursor?: string | null
  size?: number
}) {
  const search = new URLSearchParams()
  if (params.cursor) search.set('cursor', params.cursor)
  search.set('size', String(params.size ?? 20))

  const res = await apiFetch(
    `/api/community/posts/${params.postId}/comments?${search.toString()}`,
  )
  return (await res.json()) as CommunityCommentCursorListResponse
}

export async function createCommunityComment(params: {
  postId: number
  body: CreateCommunityCommentRequest
}) {
  const res = await apiFetch(`/api/community/posts/${params.postId}/comments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params.body),
  })
  return (await res.json()) as CommunityCommentMutationResponse
}

