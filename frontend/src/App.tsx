import { HashRouter } from 'react-router-dom'
import AppRouter from './router/AppRouter'
import { AuthProvider } from './features/auth'

function App() {
  return (
    <HashRouter>
      <AuthProvider>
        <AppRouter />
      </AuthProvider>
    </HashRouter>
  )
}

export default App
