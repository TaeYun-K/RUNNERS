/// <reference types="vite/client" />
/* eslint-disable @typescript-eslint/no-unused-vars */

interface ImportMetaEnv {
  readonly VITE_GOOGLE_CLIENT_ID?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

type GoogleIdInitializeOptions = {
  client_id: string
  callback: (response: { credential?: string }) => void
}

type GoogleIdRenderButtonOptions = Record<string, unknown>

declare global {
  interface Window {
    google?: {
      accounts?: {
        id?: {
          initialize: (options: GoogleIdInitializeOptions) => void
          renderButton: (
            parent: HTMLElement,
            options: GoogleIdRenderButtonOptions,
          ) => void
        }
      }
    }
  }
}

export {}
