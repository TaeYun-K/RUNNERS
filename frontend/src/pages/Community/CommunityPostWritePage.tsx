import { useEffect, useMemo, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ArrowLeft, X } from 'lucide-react'
import {
  COMMUNITY_BOARD_LABEL,
  type CommunityPostBoardType,
  fetchCommunityPostDetail,
  presignCommunityPostImageUploads,
  putFileToPresignedUrl,
  useCreateCommunityPost,
  useUpdateCommunityPost,
} from '../../features/community/post'

type ExistingImage = {
  id: string
  kind: 'existing'
  key: string
  url: string
}

type NewImage = {
  id: string
  kind: 'new'
  file: File
  previewUrl: string
}

type ImageItem = ExistingImage | NewImage

const MAX_IMAGES = 10
const MAX_IMAGE_BYTES = 10 * 1024 * 1024

export function CommunityPostWritePage() {
  const params = useParams()
  const navigate = useNavigate()

  const postId = useMemo(() => {
    const raw = params.postId
    if (!raw) return null
    const parsed = Number.parseInt(raw, 10)
    return Number.isFinite(parsed) ? parsed : null
  }, [params.postId])

  const isEdit = postId != null

  const { creating, error: createError, create } = useCreateCommunityPost()
  const { updating, error: updateError, update } = useUpdateCommunityPost()

  const [boardType, setBoardType] = useState<CommunityPostBoardType>('FREE')
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [images, setImages] = useState<ImageItem[]>([])
  const [uploadingImages, setUploadingImages] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)
  const [loadingPost, setLoadingPost] = useState(false)
  const fileInputRef = useRef<HTMLInputElement | null>(null)

  const newImagesRef = useRef<NewImage[]>([])
  const loadedPostIdRef = useRef<number | null>(null)

  const titleLength = useMemo(() => title.length, [title])
  const contentLength = useMemo(() => content.length, [content])

  useEffect(() => {
    newImagesRef.current = images.filter((img) => img.kind === 'new') as NewImage[]
  }, [images])

  useEffect(() => {
    return () => {
      for (const img of newImagesRef.current) URL.revokeObjectURL(img.previewUrl)
    }
  }, [])

  useEffect(() => {
    const controller = new AbortController()

    if (postId == null) {
      loadedPostIdRef.current = null
      setLoadingPost(false)
      setSubmitError(null)
      setBoardType('FREE')
      setTitle('')
      setContent('')
      setImages([])
      return () => controller.abort()
    }

    if (loadedPostIdRef.current === postId) return () => controller.abort()

    setLoadingPost(true)
    setSubmitError(null)
    ;(async () => {
      try {
        const post = await fetchCommunityPostDetail(postId)
        if (controller.signal.aborted) return

        loadedPostIdRef.current = postId
        setBoardType(post.boardType)
        setTitle(post.title ?? '')
        setContent(post.content ?? '')
        setImages(
          (post.imageUrls ?? []).map((url, idx) => ({
            id: post.imageKeys?.[idx] ?? url,
            kind: 'existing',
            key: post.imageKeys?.[idx] ?? url,
            url,
          })),
        )
      } catch (e) {
        if (!controller.signal.aborted) {
          setSubmitError(e instanceof Error ? e.message : String(e))
        }
      } finally {
        if (!controller.signal.aborted) setLoadingPost(false)
      }
    })()

    return () => controller.abort()
  }, [postId])

  const submitting = creating || updating || uploadingImages || loadingPost

  const canSubmit = useMemo(() => {
    if (submitting) return false
    if (!title.trim()) return false
    if (!content.trim()) return false
    if (titleLength > 200) return false
    if (contentLength > 16000) return false
    if (images.length > MAX_IMAGES) return false
    return true
  }, [content, contentLength, images.length, submitting, title, titleLength])

  const removeImage = (id: string) => {
    setImages((prev) => {
      const target = prev.find((p) => p.id === id)
      if (target?.kind === 'new') URL.revokeObjectURL(target.previewUrl)
      return prev.filter((p) => p.id !== id)
    })
  }

  const handlePickImages = () => {
    fileInputRef.current?.click()
  }

  const handleFilesSelected = (files: FileList | null) => {
    if (!files?.length) return

    const incoming: NewImage[] = []
    for (const file of Array.from(files)) {
      if (!file || file.size <= 0) continue
      if (!file.type || !file.type.startsWith('image/')) {
        setSubmitError('이미지 파일만 업로드할 수 있습니다.')
        continue
      }
      if (file.size > MAX_IMAGE_BYTES) {
        setSubmitError('이미지 파일은 최대 10MB까지 업로드할 수 있습니다.')
        continue
      }

      incoming.push({
        id:
          typeof crypto !== 'undefined' && 'randomUUID' in crypto
            ? crypto.randomUUID()
            : `${Date.now()}-${Math.random().toString(16).slice(2)}`,
        kind: 'new',
        file,
        previewUrl: URL.createObjectURL(file),
      })
    }

    setImages((prev) => {
      const remaining = Math.max(0, MAX_IMAGES - prev.length)
      const next = incoming.slice(0, remaining)
      for (const dropped of incoming.slice(remaining)) {
        URL.revokeObjectURL(dropped.previewUrl)
      }
      return [...prev, ...next]
    })

    if (fileInputRef.current) fileInputRef.current.value = ''
  }

  const handleSubmit = async () => {
    if (!canSubmit) return
    setSubmitError(null)
    try {
      setUploadingImages(true)

      const existingKeys = images
        .filter((img) => img.kind === 'existing')
        .map((img) => (img as ExistingImage).key)
      const newFiles = images
        .filter((img) => img.kind === 'new')
        .map((img) => (img as NewImage).file)

      const newKeys =
        newFiles.length === 0
          ? []
          : await (async () => {
              const presignedItems = await presignCommunityPostImageUploads(newFiles)
              for (let i = 0; i < presignedItems.length; i++) {
                await putFileToPresignedUrl({
                  uploadUrl: presignedItems[i].uploadUrl,
                  file: newFiles[i],
                  contentType: presignedItems[i].contentType,
                })
              }
              return presignedItems.map((p) => p.key)
            })()

      const body = {
        boardType,
        title: title.trim(),
        content: content.trim(),
        imageKeys: [...existingKeys, ...newKeys],
      }

      const res = isEdit && postId != null ? await update(postId, body) : await create(body)
      navigate(`/community/${res.postId}`, { replace: true })
    } catch (e) {
      setSubmitError(e instanceof Error ? e.message : String(e))
    } finally {
      setUploadingImages(false)
    }
  }

  return (
    <section className="mx-auto max-w-5xl rounded-2xl border border-border bg-card p-6 shadow-sm">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex items-center gap-3">
          <Link
            to={isEdit ? `/community/${postId}` : '/community'}
            className="inline-flex h-9 items-center gap-2 rounded-full border border-border bg-background px-4 text-sm font-medium text-foreground transition hover:bg-secondary/60"
          >
            <ArrowLeft className="h-4 w-4" />
            뒤로
          </Link>
          <h2 className="text-xl font-bold tracking-tight text-foreground">
            {isEdit ? '글 수정' : '글쓰기'}
          </h2>
        </div>
      </div>

      <div className="mt-6 grid gap-5 rounded-2xl border border-border bg-background p-5">
        <div className="grid gap-2">
          <p className="text-sm font-semibold text-foreground">게시판</p>
          <div className="flex flex-wrap gap-2">
            {(['FREE', 'QNA', 'INFO'] as const).map((t) => (
              <button
                key={t}
                type="button"
                onClick={() => setBoardType(t)}
                className={[
                  'h-9 rounded-full border px-4 text-sm font-medium transition',
                  t === boardType
                    ? 'border-blue-500/40 bg-blue-600/10 text-blue-600'
                    : 'border-border bg-background text-foreground hover:bg-secondary/60',
                ].join(' ')}
                disabled={submitting}
              >
                {COMMUNITY_BOARD_LABEL[t] ?? t}
              </button>
            ))}
          </div>
        </div>

        <div className="grid gap-2">
          <div className="flex items-center justify-between gap-3">
            <label className="text-sm font-semibold text-foreground" htmlFor="post-title">
              제목
            </label>
            <p className="text-xs text-muted-foreground">
              {titleLength.toLocaleString()} / 200
            </p>
          </div>
          <input
            id="post-title"
            value={title}
            onChange={(e) => setTitle(e.target.value.slice(0, 200))}
            placeholder="제목을 입력하세요"
            disabled={submitting}
            className="h-11 w-full rounded-xl border border-border bg-background px-4 text-sm text-foreground outline-none transition focus:border-blue-500/40 focus:ring-2 focus:ring-blue-500/20 disabled:cursor-not-allowed disabled:opacity-60"
          />
        </div>

        <div className="grid gap-2">
          <div className="flex items-center justify-between gap-3">
            <label className="text-sm font-semibold text-foreground" htmlFor="post-content">
              내용
            </label>
            <p className="text-xs text-muted-foreground">
              {contentLength.toLocaleString()} / 16000
            </p>
          </div>
          <textarea
            id="post-content"
            value={content}
            onChange={(e) => setContent(e.target.value.slice(0, 16000))}
            placeholder="내용을 입력하세요"
            disabled={submitting}
            className="min-h-60 w-full resize-y rounded-xl border border-border bg-background px-4 py-3 text-sm text-foreground outline-none transition focus:border-blue-500/40 focus:ring-2 focus:ring-blue-500/20 disabled:cursor-not-allowed disabled:opacity-60"
          />
        </div>

        <div className="grid gap-2">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <p className="text-sm font-semibold text-foreground">이미지</p>
            <p className="text-xs text-muted-foreground">
              {images.length.toLocaleString()} / {MAX_IMAGES.toLocaleString()} (각
              10MB)
            </p>
          </div>

          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            multiple
            onChange={(e) => handleFilesSelected(e.target.files)}
            className="hidden"
            disabled={submitting}
          />

          <div className="flex flex-wrap gap-2">
            <button
              type="button"
              onClick={handlePickImages}
              disabled={submitting || images.length >= MAX_IMAGES}
              className="inline-flex h-10 items-center justify-center rounded-xl border border-border bg-background px-4 text-sm font-semibold text-foreground transition hover:bg-secondary/60 disabled:cursor-not-allowed disabled:opacity-60"
            >
              이미지 추가
            </button>
            {images.length ? (
              <button
                type="button"
                onClick={() => {
                  setImages((prev) => {
                    for (const img of prev) {
                      if (img.kind === 'new') URL.revokeObjectURL(img.previewUrl)
                    }
                    return []
                  })
                }}
                disabled={submitting}
                className="inline-flex h-10 items-center justify-center rounded-xl border border-border bg-background px-4 text-sm font-semibold text-foreground transition hover:bg-secondary/60 disabled:cursor-not-allowed disabled:opacity-60"
              >
                모두 제거
              </button>
            ) : null}
          </div>

          {images.length ? (
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4">
              {images.map((img) => (
                <div
                  key={img.id}
                  className="relative overflow-hidden rounded-xl border border-border bg-secondary/30"
                >
                  <img
                    src={img.kind === 'new' ? img.previewUrl : img.url}
                    alt=""
                    className="aspect-square w-full object-cover"
                    loading="lazy"
                    referrerPolicy="no-referrer"
                  />
                  <button
                    type="button"
                    onClick={() => removeImage(img.id)}
                    disabled={submitting}
                    className="absolute right-2 top-2 inline-flex h-8 w-8 items-center justify-center rounded-full bg-background/90 text-foreground shadow-sm transition hover:bg-background disabled:cursor-not-allowed disabled:opacity-60"
                    aria-label="이미지 제거"
                  >
                    <X className="h-4 w-4" />
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <div className="rounded-xl border border-dashed border-border bg-background px-4 py-8 text-center">
              <p className="text-sm font-semibold text-foreground">
                이미지를 추가할 수 있어요
              </p>
              <p className="mt-1 text-xs text-muted-foreground">
                최대 {MAX_IMAGES}장, 각 10MB 이하
              </p>
            </div>
          )}
        </div>

        {createError || updateError ? (
          <div className="rounded-xl border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive">
            {createError || updateError}
          </div>
        ) : null}

        {submitError ? (
          <div className="rounded-xl border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive">
            {submitError}
          </div>
        ) : null}

        <div className="flex flex-wrap items-center justify-end gap-2">
          <Link
            to={isEdit ? `/community/${postId}` : '/community'}
            className="inline-flex h-10 items-center justify-center rounded-xl border border-border bg-background px-4 text-sm font-semibold text-foreground transition hover:bg-secondary/60"
          >
            취소
          </Link>
          <button
            type="button"
            onClick={handleSubmit}
            disabled={!canSubmit}
            className="h-10 rounded-xl bg-blue-600 px-4 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {uploadingImages
              ? '이미지 업로드 중…'
              : updating
                ? '수정 중…'
                : creating
                  ? '등록 중…'
                  : isEdit
                    ? '수정'
                    : '등록'}
          </button>
        </div>
      </div>
    </section>
  )
}
