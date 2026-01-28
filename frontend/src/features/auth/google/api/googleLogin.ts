import { apiFetch } from '../../../../shared/api/apiFetch'
import type { GoogleLoginResponse } from '../types'

export async function googleLogin(idToken: string) {
  const res = await apiFetch('/api/auth/google', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ idToken }),
  })
  return (await res.json()) as GoogleLoginResponse
}

