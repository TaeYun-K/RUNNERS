export type GoogleLoginResponse = {
  userId: number
  email: string
  name: string | null
  nickname: string | null
  picture: string | null
  accessToken: string
  isNewUser: boolean
}

