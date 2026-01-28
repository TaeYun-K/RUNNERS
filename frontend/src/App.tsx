import { HashRouter, Route, Routes } from 'react-router-dom'
import AppLayout from './app/Layout/AppLayout'
import { CommunityPage } from './app/Community/CommunityPage'
import { DashboardPage } from './app/MyPage/DashboardPage'
import { LoginPage } from './app/Auth/LoginPage'
import MainPage from './app/Home/MainPage'
import { NotFoundPage } from './app/Error/NotFoundPage'
import { ProfilePage } from './app/MyPage/ProfilePage'
import { RequireAuth } from './features/auth'

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
