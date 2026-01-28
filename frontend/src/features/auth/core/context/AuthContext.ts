import { createContext } from 'react'

export type AuthContextValue = {
  accessToken: string | null
  setAccessToken: (token: string) => void
  clearAccessToken: () => void
  bootstrapping: boolean
}

export const AuthContext = createContext<AuthContextValue | null>(null)

