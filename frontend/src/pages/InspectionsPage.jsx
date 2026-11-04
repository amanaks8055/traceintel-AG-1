import { useState } from 'react'
import { apiClient, errorMessage } from '../api/client'
import { useLoad } from '../api/useLoad'
import { useAuth } from '../auth/useAuth'
import { PageHero } from '../components/PageHero'
import { SelectField } from '../components/SelectField'
import { DataTable, formatDate, LoadState, Panel, Status } from '../components/ui'

const columns = [
  ['Lot', (inspection) => inspection.lotNumber],
  ['Result', (inspection) => <Status value={inspection.result} />],
  ['Inspector', (inspection) => inspection.inspector],
  ['Notes', (inspection) => inspection.notes || '-'],
  ['When', (inspection) => formatDate(inspection.inspectedAt)],
]

export function InspectionsPage() {
  const { user } = useAuth()
  const inspections = useLoad('/inspections')
  const [lotId, setLotId] = useState('')
  const [result, setResult] = useState('PASS')
  const [notes, setNotes] = useState('')
  const [error, setError] = useState('')

  async function record(event) {
    event.preventDefault()
    setError('')
    try {
      await apiClient.post('/inspections', { lotId: Number(lotId), result, notes: notes.trim() })
      setLotId('')
      setNotes('')
      inspections.reload()
    } catch (failure) {
      setError(errorMessage(failure))
    }
  }

  return (
    <>
      <PageHero machine="arm" eyebrow="Quality control" title="Inspections" description="Results of material lot inspections." />
      <div className="page-body">
        {['ADMIN', 'QUALITY_ENGINEER'].includes(user.role) && (
          <Panel title="Record an inspection">
            <form className="form-grid" onSubmit={record}>
              <SelectField label="Material lot" path="/lots" value={lotId} onChange={setLotId} optionLabel={(lot) => lot.lotNumber} />
              <label>Result
                <select value={result} onChange={(event) => setResult(event.target.value)}>
                  <option>PASS</option>
                  <option>FAIL</option>
                </select>
              </label>
              <label className="wide">Notes<input maxLength="500" value={notes} onChange={(event) => setNotes(event.target.value)} /></label>
              <button className="primary-button" type="submit">Save inspection</button>
            </form>
            {error && <p className="error-message">{error}</p>}
          </Panel>
        )}
        <Panel title="Inspection history">
          <LoadState state={inspections} empty="No inspections recorded yet.">
            {inspections.data && <DataTable columns={columns} rows={inspections.data} />}
          </LoadState>
        </Panel>
      </div>
    </>
  )
}
