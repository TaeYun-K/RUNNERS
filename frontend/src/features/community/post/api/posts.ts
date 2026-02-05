import { apiFetch } from '../../../../shared/api/apiFetch'
import type {
  CommunityPostBoardType,
  CommunityPostCountResponse,
  CommunityPostCursorListResponse,
  CommunityPostDetail,
  CommunityPostMutationResponse,
  CommunityPostRecommendResponse,
  CreateCommunityPostRequest,
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

export async function fetchMyPostsCount() {
  const res = await apiFetch('/api/community/posts/me/count')
  return (await res.json()) as CommunityPostCountResponse
}

export async function fetchPostsCommentedCount() {
  const res = await apiFetch('/api/community/posts/commented/count')
  return (await res.json()) as CommunityPostCountResponse
}

export async function fetchMyPosts(params: {
  cursor?: string | null
  size?: number
}) {
  const search = new URLSearchParams()
  if (params.cursor) search.set('cursor', params.cursor)
  search.set('size', String(params.size ?? 20))
  const res = await apiFetch(`/api/community/posts/me?${search.toString()}`)
  return (await res.json()) as CommunityPostCursorListResponse
}

export async function fetchPostsCommented(params: {
  cursor?: string | null
  size?: number
}) {
  const search = new URLSearchParams()
  if (params.cursor) search.set('cursor', params.cursor)
  search.set('size', String(params.size ?? 20))
  const res = await apiFetch(
    `/api/community/posts/commented?${search.toString()}`,
  )
  return (await res.json()) as CommunityPostCursorListResponse
}

export async function fetchCommunityPostDetail(postId: number) {
  const res = await apiFetch(`/api/community/posts/${postId}`)
  return (await res.json()) as CommunityPostDetail
}

export async function createCommunityPost(params: {
  body: CreateCommunityPostRequest
}) {
  const res = await apiFetch('/api/community/posts', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params.body),
  })
  return (await res.json()) as CommunityPostMutationResponse
}

export async function updateCommunityPost(params: {
  postId: number
  body: CreateCommunityPostRequest
}) {
  const res = await apiFetch(`/api/community/posts/${params.postId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params.body),
  })
  return (await res.json()) as CommunityPostMutationResponse
}

export async function deleteCommunityPost(postId: number) {
  await apiFetch(`/api/community/posts/${postId}`, { method: 'DELETE' })
}

export async function fetchCommunityPostRecommendStatus(
  postId: number,
  params?: { signal?: AbortSignal },
) {
  const res = await apiFetch(`/api/community/posts/${postId}/recommend`, {
    signal: params?.signal,
  })
  return (await res.json()) as CommunityPostRecommendResponse
}

export async function recommendCommunityPost(
  postId: number,
  params?: { signal?: AbortSignal },
) {
  const res = await apiFetch(`/api/community/posts/${postId}/recommend`, {
    method: 'PUT',
    signal: params?.signal,
  })
  return (await res.json()) as CommunityPostRecommendResponse
}

export async function unrecommendCommunityPost(
  postId: number,
  params?: { signal?: AbortSignal },
) {
  const res = await apiFetch(`/api/community/posts/${postId}/recommend`, {
    method: 'DELETE',
    signal: params?.signal,
  })
  return (await res.json()) as CommunityPostRecommendResponse
}
