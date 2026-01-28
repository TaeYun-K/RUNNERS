import { apiFetch } from '../../../shared/api/apiFetch'
import type { MyProfile } from '../types'

type PresignUploadFileRequest = {
  fileName: string
  contentType: string
  contentLength: number
}

type PresignedUploadItem = {
  key: string
  uploadUrl: string
  fileUrl: string
  contentType: string
}

type PresignUploadResponse = {
  items: PresignedUploadItem[]
  expiresAt: string
}

export async function presignMyProfileImageUpload(file: File) {
  const body: { files: PresignUploadFileRequest[] } = {
    files: [
      {
        fileName: file.name,
        contentType: file.type || 'application/octet-stream',
        contentLength: file.size,
      },
    ],
  }

  const res = await apiFetch('/api/users/me/profile-image/presign', {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify(body),
  })
  const json = (await res.json()) as PresignUploadResponse
  if (!json.items?.length) throw new Error('Presign 응답이 올바르지 않습니다.')
  return json.items[0]
}

export async function putFileToPresignedUrl(params: {
  uploadUrl: string
  file: File
  contentType?: string
}) {
  const res = await fetch(params.uploadUrl, {
    method: 'PUT',
    headers: {
      'content-type': params.contentType ?? params.file.type ?? 'application/octet-stream',
    },
    body: params.file,
  })

  if (!res.ok) throw new Error(`이미지 업로드 실패 (HTTP ${res.status})`)
}

export async function commitMyProfileImage(key: string) {
  const res = await apiFetch('/api/users/me/profile-image/commit', {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ key }),
  })
  return (await res.json()) as MyProfile
}

export async function deleteMyProfileImage() {
  const res = await apiFetch('/api/users/me/profile-image', { method: 'DELETE' })
  return (await res.json()) as MyProfile
}

