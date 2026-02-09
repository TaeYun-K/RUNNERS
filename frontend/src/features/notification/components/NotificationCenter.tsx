import { Bell, CheckCheck, RefreshCw } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useNotifications } from '../hooks/useNotifications'
import { formatNotificationMessage } from '../utils/formatNotificationMessage'
import { formatRelativeTime } from '../utils/formatRelativeTime'

export function NotificationCenter() {
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)
  const panelRef = useRef<HTMLDivElement | null>(null)

  const {
    items,
    nextCursor,
    hasNext,
    unreadCount,
    loadingList,
    loadingMore,
    markingAll,
    error,
    refreshAll,
    loadMore,
    markAsRead,
    markAllAsRead,
  } = useNotifications({
    enabled: true,
    size: 10,
  })

  useEffect(() => {
    if (!open) return

    const onMouseDown = (event: MouseEvent) => {
      const target = event.target as Node | null
      if (target && panelRef.current?.contains(target)) return
      setOpen(false)
    }

    const onEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setOpen(false)
    }

    document.addEventListener('mousedown', onMouseDown)
    document.addEventListener('keydown', onEscape)

    return () => {
      document.removeEventListener('mousedown', onMouseDown)
      document.removeEventListener('keydown', onEscape)
    }
  }, [open])

  const toggleOpen = () => {
    const nextOpen = !open
    setOpen(nextOpen)
    if (nextOpen) void refreshAll()
  }

  const handleItemClick = async (
    notificationId: number,
    postId?: number | null,
  ) => {
    await markAsRead(notificationId)
    setOpen(false)
    if (postId) navigate(`/community/${postId}`)
  }

  return (
    <div className="relative" ref={panelRef}>
      <button
        type="button"
        onClick={toggleOpen}
        className="relative inline-flex h-10 w-10 items-center justify-center rounded-full border border-border/70 bg-background/50 text-foreground transition-colors hover:bg-muted"
        aria-label="알림센터 열기"
        aria-expanded={open}
      >
        <Bell className="h-5 w-5" />
        {unreadCount > 0 ? (
          <span className="absolute -right-1 -top-1 inline-flex min-w-5 items-center justify-center rounded-full bg-rose-500 px-1.5 text-[11px] font-semibold leading-5 text-white">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        ) : null}
      </button>

      {open ? (
        <div className="absolute right-0 top-12 z-50 w-[28rem] rounded-2xl border border-border/70 bg-background/95 p-3 shadow-xl backdrop-blur">
          <div className="mb-2 flex items-center justify-between">
            <div>
              <p className="text-sm font-semibold text-foreground">알림</p>
              <p className="text-xs text-muted-foreground">
                안 읽은 알림 {unreadCount}개
              </p>
            </div>
            <div className="flex items-center gap-1">
              <button
                type="button"
                onClick={() => void refreshAll()}
                className="inline-flex h-8 w-8 items-center justify-center rounded-full text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
                aria-label="알림 새로고침"
              >
                <RefreshCw
                  className={loadingList ? 'h-4 w-4 animate-spin' : 'h-4 w-4'}
                />
              </button>
              <button
                type="button"
                onClick={() => void markAllAsRead()}
                disabled={markingAll || unreadCount === 0}
                className="inline-flex h-8 w-8 items-center justify-center rounded-full text-muted-foreground transition-colors hover:bg-muted hover:text-foreground disabled:cursor-not-allowed disabled:opacity-50"
                aria-label="알림 모두 읽음 처리"
              >
                <CheckCheck className="h-4 w-4" />
              </button>
            </div>
          </div>

          {error ? (
            <div className="mb-2 rounded-lg border border-destructive/20 bg-destructive/10 px-3 py-2 text-xs text-destructive">
              {error}
            </div>
          ) : null}

          {loadingList ? (
            <div className="rounded-xl border border-border/60 px-3 py-6 text-center text-sm text-muted-foreground">
              알림을 불러오는 중...
            </div>
          ) : items.length === 0 ? (
            <div className="rounded-xl border border-dashed border-border/60 px-3 py-6 text-center text-sm text-muted-foreground">
              새로운 알림이 없습니다.
            </div>
          ) : (
            <>
              <ul className="max-h-80 space-y-2 overflow-y-auto pr-1">
                {items.map((item) => (
                  <li key={item.id}>
                    <button
                      type="button"
                      onClick={() =>
                        void handleItemClick(item.id, item.relatedPostId)
                      }
                      className={[
                        'w-full rounded-xl border px-3 py-2 text-left transition-colors',
                        item.isRead
                          ? 'border-border/60 bg-background hover:bg-muted/40'
                          : 'border-primary/20 bg-primary/5 hover:bg-primary/10',
                      ].join(' ')}
                    >
                      <p className="whitespace-pre-line text-sm text-foreground">
                        {formatNotificationMessage(item)}
                      </p>
                      <p className="mt-1 text-xs text-muted-foreground">
                        {formatRelativeTime(item.createdAt)}
                      </p>
                    </button>
                  </li>
                ))}
              </ul>
              <div className="mt-2 flex justify-center">
                <button
                  type="button"
                  onClick={() => void loadMore()}
                  disabled={!hasNext || !nextCursor || loadingMore}
                  className="h-8 rounded-full border border-border/70 px-4 text-xs font-medium text-foreground transition-colors hover:bg-muted disabled:cursor-not-allowed disabled:opacity-50"
                >
                  {loadingMore
                    ? '불러오는 중...'
                    : hasNext
                      ? '더 보기'
                      : '마지막입니다'}
                </button>
              </div>
            </>
          )}
        </div>
      ) : null}
    </div>
  )
}
