import { apiFetch } from '../../../shared/api/apiFetch'
import type { MyProfile, UpdateMyProfileRequest } from '../types'

export async function fetchMyProfile() {
  const res = await apiFetch('/api/users/me')
  return (await res.json()) as MyProfile
}

export async function updateMyProfile(request: UpdateMyProfileRequest) {
  const res = await apiFetch('/api/users/me/profile', {
    method: 'PATCH',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify(request),
  })
  return (await res.json()) as MyProfile
}
