import { useCallback, useState } from 'react'
import { updateCommunityPost } from '../api/posts'
import type { CreateCommunityPostRequest } from '../types'

export function useUpdateCommunityPost() {
  const [updating, setUpdating] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const update = useCallback(
    async (postId: number, body: CreateCommunityPostRequest) => {
      setUpdating(true)
      setError(null)
      try {
        return await updateCommunityPost({ postId, body })
      } catch (e) {
        setError(e instanceof Error ? e.message : String(e))
        throw e
      } finally {
        setUpdating(false)
      }
    },
    [],
  )

  return { updating, error, update }
}

