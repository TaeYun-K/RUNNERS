'use client'

import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { Menu, X } from 'lucide-react'
import { useState } from 'react'
import { useAuth } from '../../features/auth'

const navLinkBase =
  'inline-flex h-10 items-center rounded-full px-4 text-sm font-medium transition-colors ' +
  'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring ' +
  'focus-visible:ring-offset-2 focus-visible:ring-offset-background'

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  [
    navLinkBase,
    isActive
      ? 'bg-primary/10 text-primary hover:bg-primary/15'
      : 'text-muted-foreground hover:bg-muted hover:text-foreground',
  ].join(' ')

export default function AppLayout() {
  const { accessToken, clearAccessToken } = useAuth()
  const navigate = useNavigate()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)

  const handleLogout = () => {
    clearAccessToken()
    navigate('/')
  }

  const navItems = [
    { to: '/', label: '홈', end: true },
    { to: '/community', label: '커뮤니티' },
    ...(accessToken
      ? [
          { to: '/dashboard', label: '대시보드' },
          { to: '/profile', label: '프로필' },
        ]
      : []),
  ]

  return (
    <div className="min-h-screen bg-gradient-to-b from-background via-background to-primary/5 text-foreground">
      <header className="sticky top-0 z-50 border-b border-border/60 bg-background/70 backdrop-blur-xl supports-[backdrop-filter]:bg-background/60">
        <div className="mx-auto flex h-20 max-w-6xl items-center justify-between px-4">
          {/* Logo */}
          <button
            type="button"
            onClick={() => navigate('/')}
            className="-ml-2 flex items-center rounded-xl px-2 py-1 transition-colors hover:bg-muted/70 sm:-ml-50"
            aria-label="홈으로 이동"
          >
            <img src="/logo_dark.svg" alt="RUNNERS" className="h-14 w-auto sm:h-18" />
          </button>

          {/* Desktop Nav */}
          <nav className="hidden items-center gap-1 rounded-full bg-primary/5 p-1 ring-1 ring-primary/10 md:flex">
            {navItems.map((item) => (
              <NavLink key={item.to} to={item.to} end={item.end} className={navLinkClass}>
                {item.label}
              </NavLink>
            ))}
          </nav>

          {/* Auth / Mobile */}
          <div className="flex items-center gap-3">
            {accessToken ? (
              <button
                onClick={handleLogout}
                className="hidden h-10 items-center justify-center rounded-full border border-primary/20 bg-primary/5 px-4 text-sm font-medium text-foreground transition-colors hover:bg-primary/10 md:inline-flex"
              >
                로그아웃
              </button>
            ) : (
              <NavLink
                to="/login"
                className="hidden h-10 items-center justify-center rounded-full bg-primary px-4 text-sm font-semibold text-primary-foreground shadow-sm transition-colors hover:bg-primary/90 md:inline-flex"
              >
                로그인
              </NavLink>
            )}

            <button
              type="button"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="inline-flex h-10 w-10 items-center justify-center rounded-full border border-border/70 bg-background/50 text-foreground transition-colors hover:bg-muted md:hidden"
              aria-label="메뉴"
            >
              {mobileMenuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
            </button>
          </div>
        </div>

        {mobileMenuOpen && (
          <div className="border-t border-border/60 bg-background/80 px-4 py-4 backdrop-blur-xl md:hidden">
            <nav className="flex flex-col gap-2">
              {navItems.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.end}
                  className={({ isActive }) =>
                    [
                      'inline-flex h-10 items-center rounded-xl px-4 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:ring-offset-background',
                      isActive
                        ? 'bg-primary/10 text-primary ring-1 ring-primary/20 hover:bg-primary/15'
                        : 'text-muted-foreground hover:bg-muted hover:text-foreground',
                    ].join(' ')
                  }
                  onClick={() => setMobileMenuOpen(false)}
                >
                  {item.label}
                </NavLink>
              ))}
            </nav>
          </div>
        )}
      </header>

      <main className="mx-auto max-w-6xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  )
}
