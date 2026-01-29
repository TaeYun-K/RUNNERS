import { emitAuthEvent } from './authEvents'

const STORAGE_KEY = 'runners_access_token'

type TokenListener = (token: string | null) => void

const listeners = new Set<TokenListener>()

function safeReadTokenFromStorage() {
  try {
    if (typeof window === 'undefined') return null
    return window.localStorage.getItem(STORAGE_KEY)
  } catch {
    return null
  }
}

function safeWriteTokenToStorage(token: string) {
  try {
    if (typeof window === 'undefined') return
    window.localStorage.setItem(STORAGE_KEY, token)
  } catch {
    // ignore
  }
}

function safeClearTokenFromStorage() {
  try {
    if (typeof window === 'undefined') return
    window.localStorage.removeItem(STORAGE_KEY)
  } catch {
    // ignore
  }
}

let cachedToken: string | null = safeReadTokenFromStorage()

function notify(token: string | null) {
  for (const listener of listeners) listener(token)
}

export function getAccessToken() {
  return cachedToken
}

export function setAccessToken(token: string) {
  cachedToken = token
  safeWriteTokenToStorage(token)
  notify(token)
}

export function clearAccessToken() {
  cachedToken = null
  safeClearTokenFromStorage()
  notify(null)
}

export function subscribeAccessToken(listener: TokenListener) {
  listeners.add(listener)
  return () => {
    listeners.delete(listener)
  }
}

if (typeof window !== 'undefined') {
  window.addEventListener('storage', (event) => {
    if (event.key !== STORAGE_KEY) return
    const prev = cachedToken
    const next = event.newValue
    cachedToken = next
    if (prev && !next) emitAuthEvent({ type: 'logged_out', reason: 'remote_logout' })
    notify(next)
  })
}
