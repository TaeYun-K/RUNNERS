import { emitAuthEvent } from './authEvents'
import { clearAccessToken } from './token'

export function logout(reason: 'session_expired' | 'remote_logout' | 'user_logout') {
  emitAuthEvent({ type: 'logged_out', reason })
  clearAccessToken()
}

