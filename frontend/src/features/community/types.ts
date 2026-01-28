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

