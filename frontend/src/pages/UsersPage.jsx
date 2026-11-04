import { useState } from 'react'
import { apiClient, errorMessage } from '../api/client'
import { useLoad } from '../api/useLoad'
import { PageHero } from '../components/PageHero'
import { DataTable, formatDate, LoadState, Panel, Status } from '../components/ui'

const ROLES = ['ADMIN', 'OPERATOR', 'SUPERVISOR', 'QUALITY_ENGINEER', 'MANAGER', 'USER']
const emptyForm = { fullName: '', username: '', email: '', password: '', role: 'OPERATOR' }

export function UsersPage() {
  const users = useLoad('/admin/users')
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState('')
  const [resetCode, setResetCode] = useState(null)

  async function create(event) {
    event.preventDefault()
    setError('')
    try {
      await apiClient.post('/admin/users', { ...form, email: form.email || null })
      setForm(emptyForm)
      users.reload()
    } catch (failure) {
      setError(errorMessage(failure))
    }
  }

  async function update(user, changes) {
    setError('')
    try {
      await apiClient.put(`/admin/users/${user.id}`, { fullName: user.fullName, email: user.email, role: user.role, active: user.active, ...changes })
      users.reload()
    } catch (failure) {
      setError(errorMessage(failure))
    }
  }

  async function createResetCode(user) {
    setError('')
    try {
      const response = await apiClient.post(`/admin/users/${user.id}/reset-code`)
      setResetCode(response.data)
      users.reload()
    } catch (failure) {
      setError(errorMessage(failure))
    }
  }

  const columns = [
    ['Name', (user) => user.fullName],
    ['Login', (user) => user.username],
    ['Role', (user) => (
      <select aria-label={`Role for ${user.username}`} value={user.role} onChange={(event) => update(user, { role: event.target.value })}>
        {ROLES.map((role) => <option key={role} value={role}>{role.replace('_', ' ')}</option>)}
      </select>
    )],
    ['Status', (user) => (
      <div className="row-actions">
        <Status value={user.active ? 'ACTIVE' : 'INACTIVE'} />
        {user.resetRequested && <Status value="RESET REQUESTED" />}
      </div>
    )],
    ['Action', (user) => (
      <div className="row-actions">
        <button className="text-button" type="button" onClick={() => update(user, { active: !user.active })}>{user.active ? 'Disable' : 'Enable'}</button>
        {user.active && <button className="text-button" type="button" onClick={() => createResetCode(user)}>Reset password</button>}
      </div>
    )],
  ]

  return (
    <>
      <PageHero machine="assembly" eyebrow="Administration" title="Users" description="Create accounts and assign roles. New sign ups start with the USER role." />
      <div className="page-body">
        <Panel title="Add user">
          <form className="form-grid" onSubmit={create}>
            <label>Full name<input required value={form.fullName} onChange={(event) => setForm({ ...form, fullName: event.target.value })} /></label>
            <label>Username<input required minLength="3" value={form.username} onChange={(event) => setForm({ ...form, username: event.target.value })} /></label>
            <label>Email<input type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} /></label>
            <label>Password<input required type="password" minLength="8" autoComplete="new-password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} /></label>
            <label>Role
              <select value={form.role} onChange={(event) => setForm({ ...form, role: event.target.value })}>
                {ROLES.map((role) => <option key={role} value={role}>{role.replace('_', ' ')}</option>)}
              </select>
            </label>
            <button className="primary-button" type="submit">Add user</button>
          </form>
        </Panel>
        {error && <p className="error-message">{error}</p>}
        {resetCode && (
          <Panel title={`Reset code for ${resetCode.username}`}>
            <p className="reset-code">{resetCode.code}</p>
            <p>Give this code to the user in person. It will not be shown again, works once, and expires at {formatDate(resetCode.expiresAt)}.</p>
            <button className="text-button" type="button" onClick={() => setResetCode(null)}>Hide code</button>
          </Panel>
        )}
        <Panel title="All users">
          <LoadState state={users} empty="No users yet.">
            {users.data && <DataTable columns={columns} rows={users.data} />}
          </LoadState>
        </Panel>
      </div>
    </>
  )
}
