import { useCallback, useState } from 'react'
import { createCommunityPost } from '../api/posts'
import type { CreateCommunityPostRequest } from '../types'

export function useCreateCommunityPost() {
  const [creating, setCreating] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const create = useCallback(async (body: CreateCommunityPostRequest) => {
    setCreating(true)
    setError(null)
    try {
      return await createCommunityPost({ body })
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
      throw e
    } finally {
      setCreating(false)
    }
  }, [])

  return { creating, error, create }
}

