export type CommunityComment = {
  commentId: number
  postId: number
  authorId: number
  authorName: string
  authorPicture: string | null
  authorTotalDistanceKm: number | null
  parentId: number | null
  content: string
  recommendCount: number
  createdAt: string
  updatedAt: string
}

export type CommunityCommentCursorListResponse = {
  comments: CommunityComment[]
  nextCursor: string | null
}

export type CommunityCommentMutationResponse = {
  comment: CommunityComment
  commentCount: number
}

export type DeleteCommunityCommentResponse = {
  commentId: number
  postId: number
  commentCount: number
  deletedAt: string
}

export type CreateCommunityCommentRequest = {
  content: string
  parentId?: number | null
}

export type CommunityCommentRecommendResponse = {
  postId: number
  commentId: number
  recommended: boolean
  recommendCount: number
}
