import { createContext, useMemo, useState } from 'react'
import { apiClient, clearSession } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('traceintel.user')
    if (!stored || Date.now() > Number(localStorage.getItem('traceintel.expires'))) {
      clearSession()
      return null
    }
    try {
      return JSON.parse(stored)
    } catch {
      clearSession()
      return null
    }
  })

  function startSession(username, session) {
    const nextUser = { username, fullName: session.fullName, role: session.role }
    localStorage.setItem('traceintel.user', JSON.stringify(nextUser))
    localStorage.setItem('traceintel.token', session.token)
    localStorage.setItem('traceintel.expires', String(Date.now() + session.expiresIn))
    setUser(nextUser)
  }

  async function signIn(username, password) {
    const response = await apiClient.post('/auth/login', { username, password })
    startSession(username, response.data)
  }

  async function signUp(details) {
    const response = await apiClient.post('/auth/signup', details)
    startSession(details.email, response.data)
  }

  function updateName(fullName) {
    const nextUser = { ...user, fullName }
    localStorage.setItem('traceintel.user', JSON.stringify(nextUser))
    setUser(nextUser)
  }

  function signOut() {
    clearSession()
    setUser(null)
  }

  const value = useMemo(() => ({ user, signIn, signUp, signOut, updateName }), [user])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export { AuthContext }
