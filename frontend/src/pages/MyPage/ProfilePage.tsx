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
  RotateCcw,
  Save,
  Shield,
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
  
  // 편집 모드 상태 관리
  const [isEditing, setIsEditing] = useState(false)
  const [dirty, setDirty] = useState(false)
  
  const nicknameRef = useRef<HTMLInputElement | null>(null)
  const introRef = useRef<HTMLTextAreaElement | null>(null)
  const fileInputRef = useRef<HTMLInputElement | null>(null)

  const initials = (() => {
    const base = (profile?.nickname || profile?.name || '').trim()
    return base ? base.slice(0, 2).toUpperCase() : '?'
  })()

  // 수정 취소 핸들러
  const handleCancel = () => {
    setIsEditing(false)
    setDirty(false)
    clearSuccess()
  }

  return (
    <section className="mx-auto max-w-4xl animate-in fade-in duration-500">
      <div className="overflow-hidden rounded-3xl border border-border bg-card shadow-md">
        <div className="h-32 bg-gradient-to-r from-primary/10 via-primary/5 to-background" />

        <div className="relative px-6 pb-8">
          {/* 아바타 & 기본 정보 */}
          <div className="relative -mt-16 mb-6 flex items-end justify-between gap-6">
            <div className="flex items-end gap-6">
              <div className="group relative">
                {profile?.picture ? (
                  <img
                    src={profile.picture}
                    alt="프로필"
                    className="h-32 w-32 rounded-3xl border-4 border-card object-cover shadow-xl"
                    referrerPolicy="no-referrer"
                  />
                ) : (
                  <div className="flex h-32 w-32 items-center justify-center rounded-3xl border-4 border-card bg-muted text-2xl font-bold text-muted-foreground shadow-xl">
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

                <div className="pointer-events-none absolute inset-0 rounded-3xl bg-black/0 transition-colors group-hover:bg-black/10" />

                <div className="absolute bottom-2 right-2 flex items-center gap-2">
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    disabled={loading || uploadingImage}
                    className="inline-flex h-9 items-center justify-center gap-2 rounded-xl border border-border bg-background/90 px-3 text-xs font-semibold text-foreground shadow-sm backdrop-blur transition hover:bg-background disabled:cursor-not-allowed disabled:opacity-60"
                  >
                    {uploadingImage ? (
                      <>
                        <Loader2 className="h-4 w-4 animate-spin" />
                        업로드
                      </>
                    ) : (
                      <>
                        <Camera className="h-4 w-4" />
                        사진
                      </>
                    )}
                  </button>

                  {profile?.picture ? (
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
                      className="inline-flex h-9 items-center justify-center rounded-xl border border-border bg-background/90 px-3 text-xs font-semibold text-foreground shadow-sm backdrop-blur transition hover:bg-background disabled:cursor-not-allowed disabled:opacity-60"
                      aria-label="프로필 사진 삭제"
                      title="프로필 사진 삭제"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  ) : null}
                </div>
              </div>
              <div className="mb-2 space-y-1">
                <h2 className="text-2xl font-bold tracking-tight text-foreground">
                  {profile?.nickname || profile?.name || '사용자'}
                </h2>
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <Mail className="h-3.5 w-3.5" />
                  {profile?.email}
                </div>
              </div>
            </div>

            {/* 편집 모드가 아닐 때만 보여주는 '수정하기' 버튼 */}
            {!isEditing && profile && (
              <button
                onClick={() => {
                  clearSuccess()
                  setDirty(false)
                  setIsEditing(true)
                }}
                className="mb-2 flex items-center gap-2 rounded-xl border border-border bg-background px-4 py-2 text-sm font-medium transition-all hover:bg-muted"
              >
                <Pencil className="h-4 w-4" />
                프로필 수정
              </button>
            )}
          </div>

          <div className="space-y-3">
            {error && (
              <div className="flex items-center gap-2 rounded-xl border border-destructive/20 bg-destructive/10 px-4 py-3 text-sm text-destructive animate-in slide-in-from-top-2">
                <AlertCircle className="h-4 w-4" />
                {error}
              </div>
            )}
            {successMessage && (
              <div className="flex items-center gap-2 rounded-xl border border-emerald-500/20 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-600 dark:text-emerald-400 animate-in slide-in-from-top-2">
                <CheckCircle2 className="h-4 w-4" />
                {successMessage}
              </div>
            )}
          </div>

          {loading ? (
            <div className="py-20 text-center text-sm text-muted-foreground">불러오는 중...</div>
          ) : profile ? (
            <div className="mt-8 grid gap-8 lg:grid-cols-3">
              
              {/* 활동 통계 (고정) */}
              <div className="space-y-6 lg:col-span-1">
                <div>
                  <h4 className="mb-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground">내 활동 통계</h4>
                  <div className="grid gap-3">
                    <div className="flex items-center justify-between rounded-2xl border border-border bg-background/50 p-4">
                      <div className="flex items-center gap-3">
                        <div className="rounded-lg bg-primary/10 p-2 text-primary"><MapPin className="h-4 w-4" /></div>
                        <span className="text-sm font-medium">누적 거리</span>
                      </div>
                      <span className="font-bold text-foreground">{profile.totalDistanceKm?.toFixed(1) ?? '0.0'} km</span>
                    </div>
                    <div className="flex items-center justify-between rounded-2xl border border-border bg-background/50 p-4">
                      <div className="flex items-center gap-3">
                        <div className="rounded-lg bg-secondary/50 p-2 text-foreground"><Shield className="h-4 w-4" /></div>
                        <span className="text-sm font-medium">권한</span>
                      </div>
                      <span className="text-sm font-semibold text-foreground">{profile.role}</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* 편집/상세 섹션 */}
              <div className="lg:col-span-2">
                {isEditing ? (
                  /* --- 편집 모드 폼 --- */
                  <form
                    className="space-y-5 animate-in fade-in zoom-in-95 duration-200"
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
                        setIsEditing(false) // 저장 후 다시 조회 모드로
                        setDirty(false)
                      } catch {
                        return
                      }
                    }}
                  >
                    <div className="space-y-2">
                      <label className="text-sm font-semibold text-foreground">닉네임</label>
                      <input
                        ref={nicknameRef}
                        defaultValue={profile.nickname ?? ''}
                        onChange={() => setDirty(true)}
                        disabled={saving}
                        className="flex h-11 w-full rounded-xl border border-border bg-background px-4 text-sm outline-none focus:ring-2 focus:ring-primary/20 disabled:cursor-not-allowed disabled:opacity-60"
                        autoFocus
                        placeholder="2~20자, 영문/숫자/한글/_/공백"
                      />
                      <p className="text-xs text-muted-foreground">
                        닉네임은 2~20자, 영문/숫자/한글/언더스코어/공백만 허용됩니다.
                      </p>
                    </div>
                    <div className="space-y-2">
                      <label className="text-sm font-semibold text-foreground">소개글</label>
                      <textarea
                        ref={introRef}
                        defaultValue={profile.intro ?? ''}
                        onChange={() => setDirty(true)}
                        disabled={saving}
                        className="min-h-[120px] w-full resize-none rounded-xl border border-border bg-background px-4 py-3 text-sm outline-none focus:ring-2 focus:ring-primary/20 disabled:cursor-not-allowed disabled:opacity-60"
                        placeholder="최대 30자"
                      />
                      <p className="text-xs text-muted-foreground">소개는 최대 30자입니다.</p>
                    </div>
                    <div className="flex items-center gap-3">
                      <button
                        type="submit"
                        disabled={saving || !dirty}
                        className="flex h-11 flex-1 items-center justify-center gap-2 rounded-xl bg-primary text-sm font-bold text-primary-foreground shadow-lg shadow-primary/20 disabled:opacity-50"
                      >
                        {saving ? "저장 중..." : <><Save className="h-4 w-4" /> 저장</>}
                      </button>
                      <button
                        type="button"
                        onClick={handleCancel}
                        disabled={saving}
                        className="flex h-11 items-center justify-center gap-2 rounded-xl border border-border bg-background px-5 text-sm font-medium hover:bg-muted"
                      >
                        <RotateCcw className="h-4 w-4" /> 취소
                      </button>
                    </div>
                  </form>
                ) : (
                  /* --- 조회 모드 뷰 --- */
                  <div className="space-y-6 animate-in fade-in duration-300">
                    <div className="pt-4 border-t border-border/50">
                      <h4 className="mb-2 text-sm font-semibold text-muted-foreground">사용자 정보</h4>
                      <p className="text-sm text-foreground">
                        <span className="text-muted-foreground">닉네임: </span> {profile.nickname || profile.name}
                      </p>
                    </div>
                    <div>
                      <h4 className="mb-2 text-sm font-semibold text-muted-foreground">소개</h4>
                      <p className="min-h-[60px] text-lg leading-relaxed text-foreground">
                        {profile.intro || "작성된 소개가 없습니다. 자신을 표현해 보세요!"}
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </div>
          ) : (
            <div className="py-20 text-center text-muted-foreground">정보가 없습니다.</div>
          )}
        </div>
      </div>
    </section>
  )
}
