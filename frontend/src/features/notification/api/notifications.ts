import { apiFetch } from '../../../shared/api/apiFetch'
import type {
  NotificationCursorListResponse,
  UnreadNotificationCountResponse,
} from '../types'

export async function fetchNotifications(params: {
  cursor?: string | null
  size?: number
}) {
  const search = new URLSearchParams()
  if (params.cursor) search.set('cursor', params.cursor)
  search.set('size', String(params.size ?? 20))

  const res = await apiFetch(`/api/notifications?${search.toString()}`)
  return (await res.json()) as NotificationCursorListResponse
}

export async function fetchUnreadNotificationCount() {
  const res = await apiFetch('/api/notifications/unread-count')
  return (await res.json()) as UnreadNotificationCountResponse
}

export async function markNotificationAsRead(notificationId: number) {
  await apiFetch(`/api/notifications/${notificationId}/read`, {
    method: 'PUT',
  })
}

export async function markAllNotificationsAsRead() {
  await apiFetch('/api/notifications/read-all', {
    method: 'PUT',
  })
}
