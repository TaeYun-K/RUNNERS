'use client';

import { useRef, useState } from 'react'
import { useMyProfile } from '../../features/MyPage/hooks/useMyProfile'
import { useTransientMessage } from '../../features/MyPage/hooks/useTransientMessage'
import {
  AlertCircle,
  Camera,
  CheckCircle2,
  Loader2,
  Mail,
  MapPin,
  Pencil,
  X,
  Trash2,
} from 'lucide-react'

export function ProfilePage() {
  const {
    profile,
    loading,
    saving,
    uploadingImage,
    error,
    updateProfile,
    uploadProfileImage,
    removeProfileImage,
  } = useMyProfile()
  const { message: successMessage, show: showSuccess, clear: clearSuccess } =
    useTransientMessage(3000)

  const [isEditing, setIsEditing] = useState(false)
  const [dirty, setDirty] = useState(false)

  const nicknameRef = useRef<HTMLInputElement | null>(null)
  const introRef = useRef<HTMLTextAreaElement | null>(null)
  const fileInputRef = useRef<HTMLInputElement | null>(null)

  const initials = (() => {
    const base = (profile?.nickname || profile?.name || '').trim()
    return base ? base.slice(0, 2).toUpperCase() : '?'
  })()

  const handleCancel = () => {
    setIsEditing(false)
    setDirty(false)
    clearSuccess()
  }

  return (
    <section className="mx-auto max-w-2xl">
      <div className="rounded-2xl border border-border bg-card">
        {/* Header */}
        <div className="border-b border-border px-6 py-5">
          <h1 className="text-xl font-bold tracking-tight text-foreground">프로필</h1>
          <p className="mt-1 text-sm text-muted-foreground">
            프로필 정보를 관리하세요
          </p>
        </div>

        <div className="p-6">
          {/* Alerts */}
          <div className="space-y-3">
            {error && (
              <div className="flex items-center gap-2 rounded-lg border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive">
                <AlertCircle className="h-4 w-4 flex-shrink-0" />
                {error}
              </div>
            )}
            {successMessage && (
              <div className="flex items-center gap-2 rounded-lg border border-emerald-500/20 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-600">
                <CheckCircle2 className="h-4 w-4 flex-shrink-0" />
                {successMessage}
              </div>
            )}
          </div>

          {loading ? (
            <div className="py-16 text-center text-sm text-muted-foreground">
              불러오는 중...
            </div>
          ) : profile ? (
            <div className="mt-6 space-y-8">
              {/* Avatar Section */}
              <div className="flex items-start gap-5">
                <div className="relative">
                  {profile?.picture ? (
                    <img
                      src={profile.picture || "/placeholder.svg"}
                      alt="프로필"
                      className="h-20 w-20 rounded-xl object-cover"
                      referrerPolicy="no-referrer"
                    />
                  ) : (
                    <div className="flex h-20 w-20 items-center justify-center rounded-xl bg-secondary text-xl font-bold text-muted-foreground">
                      {initials}
                    </div>
                  )}

                  <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={async (e) => {
                      clearSuccess()
                      const file = e.target.files?.[0]
                      e.target.value = ''
                      if (!file) return
                      try {
                        await uploadProfileImage(file)
                        showSuccess('프로필 사진이 변경되었습니다.')
                      } catch {
                        return
                      }
                    }}
                  />
                </div>

                <div className="flex-1 space-y-3">
                  <div>
                    <p className="text-lg font-semibold text-foreground">
                      {profile?.nickname || profile?.name || '사용자'}
                    </p>
                    <div className="mt-1 flex items-center gap-1.5 text-sm text-muted-foreground">
                      <Mail className="h-3.5 w-3.5" />
                      {profile?.email}
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => fileInputRef.current?.click()}
                      disabled={loading || uploadingImage}
                      className="inline-flex h-9 items-center gap-2 rounded-lg border border-border px-3 text-sm font-medium text-foreground transition-colors hover:bg-secondary disabled:opacity-60"
                    >
                      {uploadingImage ? (
                        <>
                          <Loader2 className="h-4 w-4 animate-spin" />
                          업로드 중
                        </>
                      ) : (
                        <>
                          <Camera className="h-4 w-4" />
                          사진 변경
                        </>
                      )}
                    </button>

                    {profile?.picture && (
                      <button
                        type="button"
                        onClick={async () => {
                          clearSuccess()
                          try {
                            await removeProfileImage()
                            showSuccess('프로필 사진이 삭제되었습니다.')
                          } catch {
                            return
                          }
                        }}
                        disabled={loading || uploadingImage}
                        className="inline-flex h-9 items-center justify-center rounded-lg border border-border px-3 text-sm font-medium text-muted-foreground transition-colors hover:bg-secondary hover:text-foreground disabled:opacity-60"
                        aria-label="프로필 사진 삭제"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    )}
                  </div>
                </div>
              </div>

              {/* Stats */}
              <div className="grid grid-cols-2 gap-4">
                <div className="rounded-xl bg-secondary/50 p-4">
                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <MapPin className="h-4 w-4" />
                    누적 거리
                  </div>
                  <p className="mt-1 text-xl font-bold text-foreground">
                    {profile.totalDistanceKm?.toFixed(1) ?? '0.0'} km
                  </p>
                </div>
                <div className="rounded-xl bg-secondary/50 p-4">
                  <div className="text-sm text-muted-foreground">권한</div>
                  <p className="mt-1 text-xl font-bold text-foreground">
                    {profile.role}
                  </p>
                </div>
              </div>

              {/* Profile Info / Edit Form */}
              {isEditing ? (
                <form
                  className="space-y-5"
                  onSubmit={async (e) => {
                    e.preventDefault()
                    clearSuccess()
                    try {
                      const nextNickname = (nicknameRef.current?.value ?? '').trim()
                      const nextIntro = (introRef.current?.value ?? '').trim()
                      await updateProfile({
                        nickname: nextNickname || null,
                        intro: nextIntro || null,
                      })
                      showSuccess('성공적으로 변경되었습니다.')
                      setIsEditing(false)
                      setDirty(false)
                    } catch {
                      return
                    }
                  }}
                >
                  <div className="space-y-2">
                    <label className="text-sm font-medium text-foreground">
                      닉네임
                    </label>
                    <input
                      ref={nicknameRef}
                      defaultValue={profile.nickname ?? ''}
                      onChange={() => setDirty(true)}
                      disabled={saving}
                      className="flex h-11 w-full rounded-lg border border-border bg-background px-4 text-sm outline-none transition-colors focus:border-foreground disabled:opacity-60"
                      autoFocus
                      placeholder="2~20자, 영문/숫자/한글/_/공백"
                    />
                  </div>

                  <div className="space-y-2">
                    <label className="text-sm font-medium text-foreground">
                      소개글
                    </label>
                    <textarea
                      ref={introRef}
                      defaultValue={profile.intro ?? ''}
                      onChange={() => setDirty(true)}
                      disabled={saving}
                      className="min-h-[100px] w-full resize-none rounded-lg border border-border bg-background px-4 py-3 text-sm outline-none transition-colors focus:border-foreground disabled:opacity-60"
                      placeholder="최대 30자"
                    />
                  </div>

                  <div className="flex items-center gap-3">
                    <button
                      type="submit"
                      disabled={saving || !dirty}
                      className="flex h-10 flex-1 items-center justify-center rounded-lg bg-foreground text-sm font-semibold text-background transition-colors hover:bg-foreground/90 disabled:opacity-50"
                    >
                      {saving ? '저장 중...' : '저장'}
                    </button>
                    <button
                      type="button"
                      onClick={handleCancel}
                      disabled={saving}
                      className="flex h-10 items-center justify-center gap-2 rounded-lg border border-border px-4 text-sm font-medium text-foreground transition-colors hover:bg-secondary"
                    >
                      <X className="h-4 w-4" />
                      취소
                    </button>
                  </div>
                </form>
              ) : (
                <div className="space-y-5">
                  <div className="flex items-center justify-between">
                    <h3 className="text-sm font-medium text-muted-foreground">
                      프로필 정보
                    </h3>
                    <button
                      onClick={() => {
                        clearSuccess()
                        setDirty(false)
                        setIsEditing(true)
                      }}
                      className="inline-flex items-center gap-1.5 text-sm font-medium text-foreground transition-colors hover:text-muted-foreground"
                    >
                      <Pencil className="h-3.5 w-3.5" />
                      수정
                    </button>
                  </div>

                  <div className="space-y-4">
                    <div>
                      <p className="text-xs text-muted-foreground">닉네임</p>
                      <p className="mt-1 text-sm text-foreground">
                        {profile.nickname || profile.name || '-'}
                      </p>
                    </div>
                    <div>
                      <p className="text-xs text-muted-foreground">소개</p>
                      <p className="mt-1 text-sm text-foreground">
                        {profile.intro || '작성된 소개가 없습니다.'}
                      </p>
                    </div>
                  </div>
                </div>
              )}
            </div>
          ) : (
            <div className="py-16 text-center text-muted-foreground">
              정보가 없습니다.
            </div>
          )}
        </div>
      </div>
    </section>
  )
}
