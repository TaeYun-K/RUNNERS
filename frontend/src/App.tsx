import { HashRouter, Route, Routes } from 'react-router-dom'
import './App.css'
import AppLayout from './layout/AppLayout'
import { CommunityPage } from './pages/Community/CommunityPage'
import { DashboardPage } from './pages/MyPage/DashboardPage'
import { LoginPage } from './pages/Login/LoginPage'
import MainPage from './pages/Home/MainPage'
import { NotFoundPage } from './pages/NotFoundPage'
import { ProfilePage } from './pages/MyPage/ProfilePage'
import { RequireAuth } from './utils/auth/RequireAuth'

function App() {
  return (
    <HashRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<AppLayout />}>
          <Route index element={<MainPage />} />
          <Route path="community" element={<CommunityPage />} />
          <Route
            path="dashboard"
            element={
              <RequireAuth>
                <DashboardPage />
              </RequireAuth>
            }
          />
          <Route
            path="profile"
            element={
              <RequireAuth>
                <ProfilePage />
              </RequireAuth>
            }
          />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </HashRouter>
  )
}

export default App
