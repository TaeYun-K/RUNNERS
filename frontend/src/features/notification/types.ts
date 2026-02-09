export type NotificationType =
  | 'COMMENT_ON_MY_POST'
  | 'COMMENT_ON_MY_COMMENTED_POST'
  | 'REPLY_TO_MY_COMMENT'
  | 'RECOMMEND_ON_MY_POST'
  | 'RECOMMEND_ON_MY_COMMENT'
  | 'UNKNOWN'

export type NotificationItem = {
  id: number
  type: NotificationType
  relatedPostId: number | null
  relatedCommentId: number | null
  postTitlePreview: string | null
  commentPreview: string | null
  actorId: number | null
  actorName: string | null
  actorPicture: string | null
  isRead: boolean
  createdAt: string
  readAt: string | null
}

export type NotificationCursorListResponse = {
  notifications: NotificationItem[]
  hasNext: boolean
  nextCursor: string | null
}

export type UnreadNotificationCountResponse = {
  unreadCount: number
}
