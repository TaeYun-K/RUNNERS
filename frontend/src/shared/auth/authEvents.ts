export type AuthEvent =
  | {
      type: 'logged_out'
      reason: 'session_expired' | 'remote_logout' | 'user_logout'
    }

type AuthEventListener = (event: AuthEvent) => void

const listeners = new Set<AuthEventListener>()

export function emitAuthEvent(event: AuthEvent) {
  for (const listener of listeners) listener(event)
}

export function subscribeAuthEvent(listener: AuthEventListener) {
  listeners.add(listener)
  return () => {
    listeners.delete(listener)
  }
}
