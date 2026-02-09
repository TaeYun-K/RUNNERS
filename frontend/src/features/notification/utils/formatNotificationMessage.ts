import type { NotificationItem } from '../types'

export function formatNotificationMessage(item: NotificationItem) {
  const actor = item.actorName?.trim() || '누군가'
  const preview = formatPreview(item)

  switch (item.type) {
    case 'COMMENT_ON_MY_POST':
      return withPreview(
        `${actor}님이 내 게시글에 댓글을 남겼습니다.`,
        preview,
      )
    case 'COMMENT_ON_MY_COMMENTED_POST':
      return withPreview(
        `${actor}님이 내가 댓글 단 글에 새 댓글을 남겼습니다.`,
        preview,
      )
    case 'REPLY_TO_MY_COMMENT':
      return withPreview(`${actor}님이 내 댓글에 답글을 남겼습니다.`, preview)
    case 'RECOMMEND_ON_MY_POST':
      return withPreview(`${actor}님이 내 글을 추천했어요.`, preview)
    case 'RECOMMEND_ON_MY_COMMENT':
      return withPreview(`${actor}님이 내 댓글을 추천했어요.`, preview)
    case 'UNKNOWN':
      return withPreview(`${actor}님의 새로운 알림이 도착했습니다.`, preview)
    default:
      return withPreview(`${actor}님의 새로운 알림이 도착했습니다.`, preview)
  }
}

function formatPreview(item: NotificationItem): string | null {
  const post = truncate(clean(item.postTitlePreview), 20)
  const comment = truncate(clean(item.commentPreview), 40)

  switch (item.type) {
    case 'COMMENT_ON_MY_POST':
    case 'COMMENT_ON_MY_COMMENTED_POST':
      return comment ? `💬 ${comment}` : post
    case 'REPLY_TO_MY_COMMENT':
      return comment ? `RE: ${comment}` : null
    case 'RECOMMEND_ON_MY_POST':
      return post ? `👍 추천: ${post}` : null
    case 'RECOMMEND_ON_MY_COMMENT':
      return comment ? `👍 추천한 댓글: ${comment}` : null
    default:
      return post ?? comment
  }
}

function withPreview(message: string, preview: string | null): string {
  return preview ? `${message}\n${preview}` : message
}

function clean(value: string | null | undefined): string | null {
  const normalized = value?.trim()
  return normalized ? normalized : null
}

function truncate(value: string | null, max: number): string | null {
  if (!value) return null
  return value.length > max ? `${value.slice(0, max)}...` : value
}
