export type CommunityPostBoardType = 'FREE' | 'QNA' | 'INFO'

export type CreateCommunityPostRequest = {
  title: string
  content: string
  imageKeys?: string[] | null
  boardType: CommunityPostBoardType
}

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

export type CommunityPostCountResponse = {
  count: number
}

export type CommunityPostDetail = {
  postId: number
  authorId: number
  authorName: string
  authorPicture: string | null
  authorTotalDistanceKm: number | null
  boardType: CommunityPostBoardType
  title: string
  content: string
  imageKeys: string[]
  imageUrls: string[]
  viewCount: number
  recommendCount: number
  commentCount: number
  createdAt: string
  updatedAt: string
}

export type CommunityPostMutationResponse = {
  postId: number
  authorId: number
  authorName: string
  authorPicture: string | null
  boardType: CommunityPostBoardType
  title: string
  content: string
  viewCount: number
  recommendCount: number
  commentCount: number
  createdAt: string
  imageUrls: string[]
}

export type CommunityPostRecommendResponse = {
  postId: number
  recommended: boolean
  recommendCount: number
}
