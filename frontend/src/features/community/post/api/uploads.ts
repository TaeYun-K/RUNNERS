import { apiFetch } from '../../../../shared/api/apiFetch'

type PresignUploadFileRequest = {
  fileName: string
  contentType: string
  contentLength: number
}

export type PresignedCommunityUploadItem = {
  key: string
  uploadUrl: string
  fileUrl: string
  contentType: string
}

type PresignUploadResponse = {
  items: PresignedCommunityUploadItem[]
  expiresAt: string
}

export async function presignCommunityPostImageUploads(files: File[]) {
  const body: { files: PresignUploadFileRequest[] } = {
    files: files.map((file) => ({
      fileName: file.name,
      contentType: file.type || 'application/octet-stream',
      contentLength: file.size,
    })),
  }

  const res = await apiFetch('/api/community/uploads/presign', {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify(body),
  })
  const json = (await res.json()) as PresignUploadResponse
  if (!json.items?.length) throw new Error('Presign 응답이 올바르지 않습니다.')
  if (json.items.length !== files.length) {
    throw new Error('Presign 응답의 파일 개수가 올바르지 않습니다.')
  }
  return json.items
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

