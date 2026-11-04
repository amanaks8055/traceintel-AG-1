import { useState } from 'react'
import { apiClient, errorMessage } from '../api/client'
import { useAuth } from '../auth/useAuth'
import { PageHero } from '../components/PageHero'
import { Panel } from '../components/ui'

export function SettingsPage() {
  const { user, updateName } = useAuth()
  const [fullName, setFullName] = useState(user.fullName)
  const [passwords, setPasswords] = useState({ currentPassword: '', newPassword: '', confirm: '' })
  const [notice, setNotice] = useState({ text: '', isError: false })

  async function run(action, text) {
    setNotice({ text: '', isError: false })
    try {
      await action()
      setNotice({ text, isError: false })
    } catch (failure) {
      setNotice({ text: errorMessage(failure), isError: true })
    }
  }

  function saveProfile(event) {
    event.preventDefault()
    run(async () => {
      const response = await apiClient.put('/account', { fullName })
      updateName(response.data.fullName)
    }, 'Profile saved.')
  }

  function savePassword(event) {
    event.preventDefault()
    if (passwords.newPassword !== passwords.confirm) {
      setNotice({ text: 'New passwords do not match.', isError: true })
      return
    }
    run(async () => {
      await apiClient.put('/account/password', { currentPassword: passwords.currentPassword, newPassword: passwords.newPassword })
      setPasswords({ currentPassword: '', newPassword: '', confirm: '' })
    }, 'Password changed.')
  }

  return (
    <>
      <PageHero machine="assembly" eyebrow="Account" title="Settings" description={`Signed in as ${user.username}, role ${user.role.replace('_', ' ')}.`} />
      <div className="page-body">
        {notice.text && <p className={notice.isError ? 'error-message' : 'success-message'}>{notice.text}</p>}
        <Panel title="Profile">
          <form className="inline-form" onSubmit={saveProfile}>
            <label>Full name<input required maxLength="100" value={fullName} onChange={(event) => setFullName(event.target.value)} /></label>
            <button className="primary-button" type="submit">Save profile</button>
          </form>
        </Panel>
        <Panel title="Change password">
          <form className="form-grid" onSubmit={savePassword}>
            <label className="wide">Current password<input required type="password" autoComplete="current-password" value={passwords.currentPassword} onChange={(event) => setPasswords({ ...passwords, currentPassword: event.target.value })} /></label>
            <label>New password<input required type="password" minLength="8" autoComplete="new-password" value={passwords.newPassword} onChange={(event) => setPasswords({ ...passwords, newPassword: event.target.value })} /></label>
            <label>Confirm new password<input required type="password" minLength="8" autoComplete="new-password" value={passwords.confirm} onChange={(event) => setPasswords({ ...passwords, confirm: event.target.value })} /></label>
            <button className="primary-button" type="submit">Change password</button>
          </form>
        </Panel>
      </div>
    </>
  )
}
