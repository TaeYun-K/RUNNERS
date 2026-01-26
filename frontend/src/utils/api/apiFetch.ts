import { getAccessToken, setAccessToken } from '../auth/token'

type ApiFetchOptions = Omit<RequestInit, 'headers'> & {
  headers?: Record<string, string>
}

async function parseErrorMessage(res: Response) {
  const contentType = res.headers.get('content-type') ?? ''
  if (contentType.includes('application/json')) {
    try {
      const json = (await res.json()) as { message?: string }
      if (json?.message) return json.message
    } catch {
      // ignore
    }
  }
  const text = await res.text().catch(() => '')
  return text || `HTTP ${res.status}`
}

async function refreshAccessToken() {
  const res = await fetch('/api/auth/refresh', {
    method: 'POST',
    credentials: 'include',
  })

  if (!res.ok) return null
  const json = (await res.json()) as { accessToken?: string }
  if (!json.accessToken) return null
  setAccessToken(json.accessToken)
  return json.accessToken
}

export async function apiFetch(input: string, init: ApiFetchOptions = {}) {
  const token = getAccessToken()
  const headers: Record<string, string> = { ...(init.headers ?? {}) }
  if (token) headers.Authorization = `Bearer ${token}`

  const res = await fetch(input, {
    ...init,
    headers,
    credentials: 'include',
  })

  if (res.status !== 401) {
    if (!res.ok) throw new Error(await parseErrorMessage(res))
    return res
  }

  const newToken = await refreshAccessToken()
  if (!newToken) throw new Error(await parseErrorMessage(res))

  const retryHeaders: Record<string, string> = { ...(init.headers ?? {}) }
  retryHeaders.Authorization = `Bearer ${newToken}`

  const retry = await fetch(input, {
    ...init,
    headers: retryHeaders,
    credentials: 'include',
  })

  if (!retry.ok) throw new Error(await parseErrorMessage(retry))
  return retry
}

