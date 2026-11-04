import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { errorMessage } from '../api/client'
import { MachineStage } from '../components/MachineStage'

export function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const { signIn } = useAuth()
  const navigate = useNavigate()

  async function handleSubmit(event) {
    event.preventDefault()
    if (!username.trim() || !password) {
      setError('Enter your email or username and password.')
      return
    }
    setError('')
    setIsSubmitting(true)
    try {
      await signIn(username.trim(), password)
      navigate('/dashboard')
    } catch (failure) {
      const refused = failure.response?.status === 401
      setError(refused ? 'Incorrect username or password.' : errorMessage(failure))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="auth-page">
      <MachineStage machine="arm" />
      <section className="auth-form">
        <Link className="brand" to="/">TraceIntel</Link>
        <h1>Sign in</h1>
        <form onSubmit={handleSubmit}>
          <label htmlFor="username">Email or username</label>
          <input id="username" autoComplete="username" value={username} onChange={(event) => setUsername(event.target.value)} />
          <label htmlFor="password">Password</label>
          <input id="password" type="password" autoComplete="current-password" value={password} onChange={(event) => setPassword(event.target.value)} />
          {error && <p className="error-message">{error}</p>}
          <button className="primary-button" disabled={isSubmitting} type="submit">
            {isSubmitting ? 'Signing in...' : 'Sign in'}
          </button>
        </form>
        <p className="auth-switch"><Link to="/forgot-password">Forgot password?</Link></p>
        <p className="auth-switch">New here? <Link to="/signup">Create an account</Link></p>
      </section>
    </main>
  )
}
