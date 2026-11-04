import { useState } from 'react'
import { apiClient, errorMessage } from '../api/client'
import { useLoad } from '../api/useLoad'
import { useAuth } from '../auth/useAuth'
import { PageHero } from '../components/PageHero'
import { SelectField } from '../components/SelectField'
import { DataTable, formatDate, LoadState, Panel, Status } from '../components/ui'

const REQUEST_ROLES = ['ADMIN', 'SUPERVISOR', 'QUALITY_ENGINEER']
const APPROVE_ROLES = ['ADMIN', 'MANAGER', 'QUALITY_ENGINEER']

function RequestHold({ onDone }) {
  const [lotId, setLotId] = useState('')
  const [reason, setReason] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function submit(event) {
    event.preventDefault()
    setError('')
    setIsSubmitting(true)
    try {
      await apiClient.post('/holds/request', { lotId: Number(lotId), reason: reason.trim() })
      setLotId('')
      setReason('')
      onDone()
    } catch (failure) {
      setError(errorMessage(failure))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Panel title="Request a hold">
      <form className="form-grid" onSubmit={submit}>
        <SelectField label="Material lot" path="/lots" value={lotId} onChange={setLotId} optionLabel={(lot) => `${lot.lotNumber} (${lot.status})`} />
        <label className="wide">Reason<input required minLength="10" maxLength="500" value={reason} onChange={(event) => setReason(event.target.value)} /></label>
        <button className="primary-button" disabled={isSubmitting} type="submit">{isSubmitting ? 'Sending...' : 'Request hold'}</button>
      </form>
      {error && <p className="error-message">{error}</p>}
    </Panel>
  )
}

function ResolveHold({ hold, onDone }) {
  const [notes, setNotes] = useState('')
  const [error, setError] = useState('')

  async function resolve(approved) {
    setError('')
    try {
      await apiClient.put(`/holds/${hold.id}/approve`, { approved, notes: notes.trim() })
      onDone()
    } catch (failure) {
      setError(errorMessage(failure))
    }
  }

  return (
    <div className="approve-cell">
      <input aria-label="Decision notes" placeholder="Notes" maxLength="500" value={notes} onChange={(event) => setNotes(event.target.value)} />
      <button className="primary-button compact-button" type="button" onClick={() => resolve(true)}>Approve</button>
      <button className="text-button" type="button" onClick={() => resolve(false)}>Reject</button>
      {error && <p className="error-message">{error}</p>}
    </div>
  )
}

function LotDecision({ lot, onDone }) {
  const [reason, setReason] = useState('')
  const [error, setError] = useState('')

  async function decide(action) {
    setError('')
    try {
      await apiClient.put(`/lots/${lot.id}/${action}`, { reason: reason.trim() })
      onDone()
    } catch (failure) {
      setError(errorMessage(failure))
    }
  }

  function reject(event) {
    if (event.currentTarget.form.reportValidity() && window.confirm(`Reject lot ${lot.lotNumber}? This cannot be undone.`)) {
      decide('reject')
    }
  }

  return (
    <form className="inline-form" onSubmit={(event) => { event.preventDefault(); decide('release') }}>
      <label>{lot.lotNumber}: reason<input required minLength="10" maxLength="500" value={reason} onChange={(event) => setReason(event.target.value)} /></label>
      <button className="primary-button" type="submit">Release lot</button>
      <button className="text-button" type="button" onClick={reject}>Reject lot</button>
      {error && <p className="error-message">{error}</p>}
    </form>
  )
}

function LotsOnHold({ refresh }) {
  const lots = useLoad('/lots')
  const held = lots.data?.filter((lot) => lot.status === 'ON_HOLD') ?? []

  function done() {
    lots.reload()
    refresh()
  }

  return (
    <Panel title="Lots on hold">
      <LoadState state={lots} empty="No lots yet.">
        {held.length === 0
          ? <p className="state-note">No lots are on hold.</p>
          : held.map((lot) => <LotDecision key={lot.id} lot={lot} onDone={done} />)}
      </LoadState>
    </Panel>
  )
}

export function HoldsPage() {
  const { user } = useAuth()
  const holds = useLoad('/holds')
  const canResolve = APPROVE_ROLES.includes(user.role)
  const [changes, setChanges] = useState(0)

  function refresh() {
    holds.reload()
    setChanges(changes + 1)
  }

  const columns = [
    ['Lot', (hold) => <>{hold.lotNumber} <Status value={hold.lotStatus} /></>],
    ['Reason', (hold) => hold.requestReason],
    ['Status', (hold) => <Status value={hold.status} />],
    ['Requested by', (hold) => hold.requestedBy],
    ['Requested', (hold) => formatDate(hold.requestedAt)],
    ['Decided by', (hold) => hold.resolvedBy || '-'],
    ['Notes', (hold) => hold.approvalNotes || '-'],
    ...(canResolve ? [['Action', (hold) => (hold.status === 'REQUESTED' ? <ResolveHold hold={hold} onDone={refresh} /> : '-')]] : []),
  ]

  return (
    <>
      <PageHero machine="welder" eyebrow="Quality control" title="Hold management" description="A hold needs two people: one to request it and a different one to decide it." />
      <div className="page-body">
        {REQUEST_ROLES.includes(user.role) && <RequestHold onDone={holds.reload} />}
        {['ADMIN', 'MANAGER'].includes(user.role) && <LotsOnHold key={changes} refresh={refresh} />}
        <Panel title="Quality holds">
          <LoadState state={holds} empty="No holds have been requested.">
            {holds.data && <DataTable columns={columns} rows={holds.data} />}
          </LoadState>
        </Panel>
      </div>
    </>
  )
}
