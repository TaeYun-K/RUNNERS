import { useCallback, useEffect, useMemo, useState } from 'react'

type UseLightboxOptions = {
  resetDeps?: readonly unknown[]
}

export function useLightbox(options: UseLightboxOptions = {}) {
  const [isOpen, setIsOpen] = useState(false)
  const [url, setUrl] = useState<string | null>(null)

  const close = useCallback(() => {
    setIsOpen(false)
    setUrl(null)
  }, [])

  const open = useCallback((nextUrl: string) => {
    setUrl(nextUrl)
    setIsOpen(true)
  }, [])

  useEffect(() => {
    if (!isOpen) return

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') close()
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [close, isOpen])

  useEffect(() => {
    if (!options.resetDeps) return
    close()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, options.resetDeps)

  return useMemo(
    () => ({
      isOpen,
      url,
      open,
      close,
    }),
    [close, isOpen, open, url],
  )
}

