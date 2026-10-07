import { useState } from 'react'
import { BrowserRouter, Navigate, Route, Routes, useLocation, useNavigate } from 'react-router-dom'
import AuthPage from './AuthPage'
import Dashboard from './Dashboard'
import { clearSession, readSavedSession, saveSession } from './api'

export default function App() {
  return (
    <BrowserRouter>
      <ApplicationRoutes />
    </BrowserRouter>
  )
}

function ApplicationRoutes() {
  const [session, setSession] = useState(readSavedSession)
  const navigate = useNavigate()
  const location = useLocation()

  function handleAuthenticated(nextSession) {
    saveSession(nextSession)
    setSession(nextSession)
    navigate('/dashboard', { replace: true })
  }

  function handleLogout() {
    clearSession()
    setSession(null)
    navigate('/sign-in', { replace: true })
  }

  function authRoute(mode) {
    if (session) return <Navigate to="/dashboard" replace />
    return <AuthPage mode={mode} onAuthenticated={handleAuthenticated} />
  }

  const dashboardRoute = session
    ? <Dashboard session={session} onLogout={handleLogout} onSessionExpired={handleLogout} />
    : <Navigate to="/sign-in" replace state={{ from: location.pathname }} />

  return (
    <Routes>
      <Route path="/" element={<Navigate to={session ? '/dashboard' : '/sign-in'} replace />} />
      <Route path="/sign-in" element={authRoute('login')} />
      <Route path="/register" element={authRoute('register')} />
      <Route path="/dashboard/*" element={dashboardRoute} />
      <Route path="*" element={<Navigate to={session ? '/dashboard' : '/sign-in'} replace />} />
    </Routes>
  )
}
