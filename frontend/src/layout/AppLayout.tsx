import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../utils/auth/AuthProvider'

export default function AppLayout() {
  const { accessToken, clearAccessToken } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    clearAccessToken()
    navigate('/login')
  }

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="app-brand" onClick={() => navigate('/')}>
          RUNNERS
        </div>

        <nav className="app-nav">
          <NavLink to="/" end>
            홈
          </NavLink>
          <NavLink to="/community">커뮤니티</NavLink>
          {accessToken ? <NavLink to="/dashboard">대시보드</NavLink> : null}
          {accessToken ? <NavLink to="/profile">프로필</NavLink> : null}
        </nav>

        <div className="app-header-right">
          {accessToken ? (
            <button className="btn" onClick={handleLogout}>
              로그아웃
            </button>
          ) : (
            <NavLink className="btn btn-primary" to="/login">
              로그인
            </NavLink>
          )}
        </div>
      </header>

      <main className="app-main">
        <Outlet />
      </main>
    </div>
  )
}
