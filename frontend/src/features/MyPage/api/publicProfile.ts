import { apiFetch } from '../../../shared/api/apiFetch'
import type { UserPublicProfile } from '../types'

export async function fetchUserPublicProfile(userId: number) {
  const res = await apiFetch(`/api/users/${userId}/public-profile`)
  return (await res.json()) as UserPublicProfile
}

