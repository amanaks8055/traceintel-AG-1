import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { errorMessage } from '../api/client'
import { MachineStage } from '../components/MachineStage'

const fields = [
  ['name', 'Full name', 'text', 'name'],
  ['email', 'Email', 'email', 'email'],
  ['password', 'Password', 'password', 'new-password'],
  ['confirm', 'Confirm password', 'password', 'new-password'],
]

function validate({ name, email, password, confirm }) {
  if (!name.trim()) return 'Enter your full name.'
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return 'Enter a valid email address.'
  if (password.length < 8) return 'Password must be at least 8 characters.'
  if (password !== confirm) return 'Passwords do not match.'
  return ''
}

export function SignUpPage() {
  const [form, setForm] = useState({ name: '', email: '', password: '', confirm: '' })
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const { signUp } = useAuth()
  const navigate = useNavigate()

  async function handleSubmit(event) {
    event.preventDefault()
    const problem = validate(form)
    setError(problem)
    if (problem) return

    setIsSubmitting(true)
    try {
      await signUp({ name: form.name.trim(), email: form.email.trim(), password: form.password })
      navigate('/dashboard')
    } catch (failure) {
      setError(failure.response?.status === 409 ? 'An account with this email already exists.' : errorMessage(failure))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="auth-page">
      <MachineStage machine="welder" />
      <section className="auth-form">
        <Link className="brand" to="/">TraceIntel</Link>
        <h1>Create account</h1>
        <form onSubmit={handleSubmit} noValidate>
          {fields.map(([name, label, type, autoComplete]) => (
            <div key={name}>
              <label htmlFor={name}>{label}</label>
              <input id={name} type={type} autoComplete={autoComplete} value={form[name]} onChange={(event) => setForm({ ...form, [name]: event.target.value })} />
            </div>
          ))}
          {error && <p className="error-message">{error}</p>}
          <button className="primary-button" disabled={isSubmitting} type="submit">
            {isSubmitting ? 'Creating account...' : 'Create account'}
          </button>
        </form>
        <p className="auth-switch">Already registered? <Link to="/login">Sign in</Link></p>
      </section>
    </main>
  )
}
