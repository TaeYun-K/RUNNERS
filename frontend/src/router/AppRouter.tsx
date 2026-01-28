import { Route, Routes } from 'react-router-dom'
import AppLayout from '../app/Layout/AppLayout'
import { LoginPage } from '../pages/Auth/LoginPage'
import { CommunityPage } from '../pages/Community/CommunityPage'
import { CommunityPostDetailPage } from '../pages/Community/CommunityPostDetailPage'
import { CommunityPostWritePage } from '../pages/Community/CommunityPostWritePage'
import MainPage from '../pages/Home/MainPage'
import { NotFoundPage } from '../pages/Error/NotFoundPage'
import { DashboardPage } from '../pages/MyPage/DashboardPage'
import { ProfilePage } from '../pages/MyPage/ProfilePage'
import { UserPublicProfilePage } from '../pages/User/UserPublicProfilePage'
import { RequireAuth } from '../features/auth'

export default function AppRouter() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={<AppLayout />}>
        <Route index element={<MainPage />} />
        <Route path="community" element={<CommunityPage />} />
        <Route
          path="community/write"
          element={
            <RequireAuth>
              <CommunityPostWritePage />
            </RequireAuth>
          }
        />
        <Route path="community/:postId" element={<CommunityPostDetailPage />} />
        <Route path="users/:userId" element={<UserPublicProfilePage />} />
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
  )
}
