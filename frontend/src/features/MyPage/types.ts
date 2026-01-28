export type MyProfile = {
  userId: number
  email: string
  name: string
  nickname: string
  intro: string | null
  picture: string | null
  role: string
  totalDistanceKm: number | null
}

export type UpdateMyProfileRequest = {
  nickname?: string | null
  intro?: string | null
}
