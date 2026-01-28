export async function refreshAccessToken() {
  const res = await fetch('/api/auth/refresh', {
    method: 'POST',
    credentials: 'include',
  })
  if (!res.ok) return null
  const json = (await res.json()) as { accessToken?: string }
  return json.accessToken ?? null
}

