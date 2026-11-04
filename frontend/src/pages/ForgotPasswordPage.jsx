import { useState } from 'react'
import { Link } from 'react-router-dom'
import { apiClient, errorMessage } from '../api/client'
import { MachineStage } from '../components/MachineStage'

function validate({ login, code, newPassword, confirm }) {
  if (!login.trim()) return 'Enter your email or username.'
  if (!code.trim()) return 'Enter the reset code from your admin.'
  if (newPassword.length < 8) return 'Password must be at least 8 characters.'
  if (newPassword !== confirm) return 'Passwords do not match.'
  return ''
}

export function ForgotPasswordPage() {
  const [form, setForm] = useState({ login: '', code: '', newPassword: '', confirm: '' })
  const [step, setStep] = useState('request')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  function field(name) {
    return { id: name, value: form[name], onChange: (event) => setForm({ ...form, [name]: event.target.value }) }
  }

  async function submit(event, problem, action, nextStep) {
    event.preventDefault()
    setError(problem)
    if (problem) return

    setIsSubmitting(true)
    try {
      await action()
      setStep(nextStep)
    } catch (failure) {
      setError(errorMessage(failure))
    } finally {
      setIsSubmitting(false)
    }
  }

  function requestReset(event) {
    const problem = form.login.trim() ? '' : 'Enter your email or username.'
    submit(event, problem, () => apiClient.post('/auth/forgot-password', { login: form.login.trim() }), 'requested')
  }

  function setNewPassword(event) {
    const body = { login: form.login.trim(), code: form.code.trim(), newPassword: form.newPassword }
    submit(event, validate(form), () => apiClient.post('/auth/reset-password', body), 'done')
  }

  if (step === 'done') {
    return (
      <main className="auth-page">
        <MachineStage machine="arm" />
        <section className="auth-form">
          <Link className="brand" to="/">TraceIntel</Link>
          <h1>Password changed</h1>
          <p className="success-message">You can now sign in with your new password.</p>
          <p className="auth-switch"><Link to="/login">Sign in</Link></p>
        </section>
      </main>
    )
  }

  return (
    <main className="auth-page">
      <MachineStage machine="arm" />
      <section className="auth-form">
        <Link className="brand" to="/">TraceIntel</Link>
        <h1>Forgot password</h1>
        {step === 'request' ? (
          <form onSubmit={requestReset} noValidate>
            <p className="auth-switch">Your admin will give you a one-time code.</p>
            <label htmlFor="login">Email or username</label>
            <input autoComplete="username" {...field('login')} />
            {error && <p className="error-message">{error}</p>}
            <button className="primary-button" disabled={isSubmitting} type="submit">
              {isSubmitting ? 'Sending...' : 'Ask admin for a code'}
            </button>
            <button className="text-button" type="button" onClick={() => { setError(''); setStep('requested') }}>I already have a code</button>
          </form>
        ) : (
          <form onSubmit={setNewPassword} noValidate>
            <p className="auth-switch">Enter the code from your admin. It works once and lasts 30 minutes from when the admin created it.</p>
            <label htmlFor="login">Email or username</label>
            <input autoComplete="username" {...field('login')} />
            <label htmlFor="code">Reset code</label>
            <input autoComplete="one-time-code" {...field('code')} />
            <label htmlFor="newPassword">New password</label>
            <input type="password" autoComplete="new-password" {...field('newPassword')} />
            <label htmlFor="confirm">Confirm new password</label>
            <input type="password" autoComplete="new-password" {...field('confirm')} />
            {error && <p className="error-message">{error}</p>}
            <button className="primary-button" disabled={isSubmitting} type="submit">
              {isSubmitting ? 'Saving...' : 'Set new password'}
            </button>
          </form>
        )}
        <p className="auth-switch">Remembered it? <Link to="/login">Sign in</Link></p>
      </section>
    </main>
  )
}
