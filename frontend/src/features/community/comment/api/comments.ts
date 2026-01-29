import { apiFetch } from '../../../../shared/api/apiFetch'
import type {
  CommunityCommentCursorListResponse,
  CommunityCommentMutationResponse,
  CommunityCommentRecommendResponse,
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

export async function fetchCommunityCommentRecommendStatus(
  postId: number,
  commentId: number,
  params?: { signal?: AbortSignal },
) {
  const res = await apiFetch(
    `/api/community/posts/${postId}/comments/${commentId}/recommend`,
    { signal: params?.signal },
  )
  return (await res.json()) as CommunityCommentRecommendResponse
}

export async function recommendCommunityComment(
  postId: number,
  commentId: number,
  params?: { signal?: AbortSignal },
) {
  const res = await apiFetch(
    `/api/community/posts/${postId}/comments/${commentId}/recommend`,
    { method: 'PUT', signal: params?.signal },
  )
  return (await res.json()) as CommunityCommentRecommendResponse
}

export async function unrecommendCommunityComment(
  postId: number,
  commentId: number,
  params?: { signal?: AbortSignal },
) {
  const res = await apiFetch(
    `/api/community/posts/${postId}/comments/${commentId}/recommend`,
    { method: 'DELETE', signal: params?.signal },
  )
  return (await res.json()) as CommunityCommentRecommendResponse
}
