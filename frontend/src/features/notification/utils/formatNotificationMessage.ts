import type { NotificationItem } from '../types'

export function formatNotificationMessage(item: NotificationItem) {
  const actor = item.actorName?.trim() || '누군가'

  switch (item.type) {
    case 'COMMENT_ON_MY_POST':
      return `${actor}님이 내 게시글에 댓글을 남겼습니다.`
    case 'COMMENT_ON_MY_COMMENTED_POST':
      return `${actor}님이 내가 댓글 단 글에 새 댓글을 남겼습니다.`
    case 'REPLY_TO_MY_COMMENT':
      return `${actor}님이 내 댓글에 답글을 남겼습니다.`
    default:
      return `${actor}님의 새로운 알림이 도착했습니다.`
  }
}
