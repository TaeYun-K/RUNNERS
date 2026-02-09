import { useCallback, useEffect, useState } from 'react'
import {
  fetchNotifications,
  fetchUnreadNotificationCount,
  markAllNotificationsAsRead,
  markNotificationAsRead,
} from '../api/notifications'
import type { NotificationItem } from '../types'

export function useNotifications(params: { enabled: boolean; size?: number }) {
  const { enabled, size = 20 } = params

  const [items, setItems] = useState<NotificationItem[]>([])
  const [nextCursor, setNextCursor] = useState<string | null>(null)
  const [hasNext, setHasNext] = useState(false)
  const [unreadCount, setUnreadCount] = useState(0)

  const [loadingList, setLoadingList] = useState(false)
  const [loadingMore, setLoadingMore] = useState(false)
  const [loadingCount, setLoadingCount] = useState(false)
  const [markingAll, setMarkingAll] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const loadUnreadCount = useCallback(
    async (signal?: AbortSignal) => {
      if (!enabled) {
        setUnreadCount(0)
        return
      }
      setLoadingCount(true)
      try {
        const json = await fetchUnreadNotificationCount()
        if (signal?.aborted) return
        setUnreadCount(Number(json.unreadCount ?? 0))
      } catch (e) {
        if (!signal?.aborted) {
          setError(e instanceof Error ? e.message : String(e))
        }
      } finally {
        if (!signal?.aborted) setLoadingCount(false)
      }
    },
    [enabled],
  )

  const loadInitial = useCallback(
    async (signal?: AbortSignal) => {
      if (!enabled) {
        setItems([])
        setNextCursor(null)
        setHasNext(false)
        return
      }

      setError(null)
      setLoadingList(true)
      try {
        const json = await fetchNotifications({ cursor: null, size })
        if (signal?.aborted) return
        setItems(json.notifications ?? [])
        setNextCursor(json.nextCursor ?? null)
        setHasNext(Boolean(json.hasNext))
      } catch (e) {
        if (!signal?.aborted) {
          setError(e instanceof Error ? e.message : String(e))
        }
      } finally {
        if (!signal?.aborted) setLoadingList(false)
      }
    },
    [enabled, size],
  )

  const loadMore = useCallback(async () => {
    if (!enabled || !hasNext || !nextCursor || loadingList || loadingMore) return

    setError(null)
    setLoadingMore(true)
    try {
      const json = await fetchNotifications({ cursor: nextCursor, size })
      setItems((prev) => [...prev, ...(json.notifications ?? [])])
      setNextCursor(json.nextCursor ?? null)
      setHasNext(Boolean(json.hasNext))
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
    } finally {
      setLoadingMore(false)
    }
  }, [enabled, hasNext, nextCursor, loadingList, loadingMore, size])

  const markAsRead = useCallback(
    async (notificationId: number) => {
      if (!enabled) return

      let wasUnread = false
      setItems((prev) =>
        prev.map((item) => {
          if (item.id !== notificationId) return item
          if (!item.isRead) wasUnread = true
          return {
            ...item,
            isRead: true,
            readAt: item.readAt ?? new Date().toISOString(),
          }
        }),
      )

      if (wasUnread) {
        setUnreadCount((prev) => Math.max(0, prev - 1))
      }

      try {
        await markNotificationAsRead(notificationId)
      } catch (e) {
        setError(e instanceof Error ? e.message : String(e))
        void loadUnreadCount()
      }
    },
    [enabled, loadUnreadCount],
  )

  const markAllAsRead = useCallback(async () => {
    if (!enabled || markingAll) return

    setMarkingAll(true)
    setError(null)

    setItems((prev) =>
      prev.map((item) => ({
        ...item,
        isRead: true,
        readAt: item.readAt ?? new Date().toISOString(),
      })),
    )
    setUnreadCount(0)

    try {
      await markAllNotificationsAsRead()
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
      void loadUnreadCount()
    } finally {
      setMarkingAll(false)
    }
  }, [enabled, loadUnreadCount, markingAll])

  const refreshAll = useCallback(
    async (signal?: AbortSignal) => {
      await Promise.all([loadInitial(signal), loadUnreadCount(signal)])
    },
    [loadInitial, loadUnreadCount],
  )

  useEffect(() => {
    if (!enabled) {
      setItems([])
      setNextCursor(null)
      setHasNext(false)
      setUnreadCount(0)
      setError(null)
      return
    }

    const controller = new AbortController()
    void loadUnreadCount(controller.signal)
    return () => controller.abort()
  }, [enabled, loadUnreadCount])

  return {
    items,
    nextCursor,
    hasNext,
    unreadCount,
    loadingList,
    loadingMore,
    loadingCount,
    markingAll,
    error,
    loadInitial,
    loadMore,
    loadUnreadCount,
    refreshAll,
    markAsRead,
    markAllAsRead,
  }
}
