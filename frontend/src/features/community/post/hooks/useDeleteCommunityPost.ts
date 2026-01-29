import { useCallback, useState } from 'react'
import { deleteCommunityPost } from '../api/posts'

export function useDeleteCommunityPost() {
  const [deleting, setDeleting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const remove = useCallback(async (postId: number) => {
    setDeleting(true)
    setError(null)
    try {
      await deleteCommunityPost(postId)
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
      throw e
    } finally {
      setDeleting(false)
    }
  }, [])

  return { deleting, error, remove }
}

