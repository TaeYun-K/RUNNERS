import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../utils/auth/AuthProvider'

const navLinkBase =
  'inline-flex items-center h-9 px-3 rounded-full text-sm font-semibold transition'

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  [
    navLinkBase,
    isActive
      ? 'bg-white text-blue-900 shadow'
      : 'text-white/90 hover:text-white hover:bg-white/10',
  ].join(' ')

export default function AppLayout() {
  const { accessToken, clearAccessToken } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    clearAccessToken()
    navigate('/login')
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="sticky top-0 z-50 h-16 px-5 flex items-center gap-4
                         bg-gradient-to-r from-blue-600 to-blue-900 shadow-lg">
        <div
          className="flex items-center cursor-pointer select-none"
          onClick={() => navigate('/')}
        >
          <img
            src="/logo.svg"
            alt="RUNNERS Logo"
            className="h-9 w-auto drop-shadow"
          />
        </div>

        <nav className="flex items-center gap-1 p-1 rounded-full
                        bg-white/10 border border-white/15 backdrop-blur">
          <NavLink to="/" end className={navLinkClass}>
            홈
          </NavLink>
          <NavLink to="/community" className={navLinkClass}>
            커뮤니티
          </NavLink>
          {accessToken ? (
            <NavLink to="/dashboard" className={navLinkClass}>
              대시보드
            </NavLink>
          ) : null}
          {accessToken ? (
            <NavLink to="/profile" className={navLinkClass}>
              프로필
            </NavLink>
          ) : null}
        </nav>

        <div className="ml-auto flex items-center gap-2">
          {accessToken ? (
            <button
              onClick={handleLogout}
              className="h-9 px-4 rounded-lg font-bold text-white
                         bg-white/10 border border-white/20
                         hover:bg-white/15 active:translate-y-px transition"
            >
              로그아웃
            </button>
          ) : (
            <NavLink
              to="/login"
              className="h-9 px-4 rounded-lg font-bold text-blue-900
                         bg-white hover:bg-white/95 transition"
            >
              로그인
            </NavLink>
          )}
        </div>
      </header>

      <main className="p-5">
        <Outlet />
      </main>
    </div>
  )
}
