import { useEffect, useState } from 'react'
import { apiClient, errorMessage } from '../api/client'
import { useLoad } from '../api/useLoad'
import { useAuth } from '../auth/useAuth'
import { PageHero } from '../components/PageHero'
import { SelectField } from '../components/SelectField'
import { DataTable, formatDate, LoadState, Panel } from '../components/ui'

const columns = [
  ['VIN', (link) => link.vin],
  ['Batch', (link) => link.batchNumber],
  ['Serial', (link) => link.serial || '-'],
  ['Customer', (link) => link.customer || '-'],
  ['Linked', (link) => formatDate(link.importedAt)],
]

function LinkVin({ onDone }) {
  const [search, setSearch] = useState('')
  const [vins, setVins] = useState([])
  const [form, setForm] = useState({ vin: '', batchId: '', customerId: '' })
  const [error, setError] = useState('')

  useEffect(() => {
    let current = true
    apiClient.get('/admin/vin/unlinked', { params: { search } })
      .then((response) => { if (current) setVins(response.data) })
      .catch((failure) => { if (current) setError(errorMessage(failure)) })
    return () => { current = false }
  }, [search])

  async function link(event) {
    event.preventDefault()
    setError('')
    try {
      await apiClient.post('/admin/vin/links', {
        vin: form.vin,
        batchId: Number(form.batchId),
        customerId: form.customerId ? Number(form.customerId) : null,
      })
      setForm({ vin: '', batchId: '', customerId: '' })
      setSearch('')
      onDone()
    } catch (failure) {
      setError(errorMessage(failure))
    }
  }

  return (
    <Panel title="Link a vehicle to a batch">
      <form className="form-grid" onSubmit={link}>
        <label>Search VINs<input value={search} onChange={(event) => setSearch(event.target.value)} /></label>
        <label>Vehicle
          <select required value={form.vin} onChange={(event) => setForm({ ...form, vin: event.target.value })}>
            <option value="">Choose...</option>
            {vins.map((vehicle) => <option key={vehicle.id} value={vehicle.vin}>{vehicle.vin} ({vehicle.customer || 'no make'})</option>)}
          </select>
        </label>
        <SelectField label="Batch" path="/batches" value={form.batchId} onChange={(batchId) => setForm({ ...form, batchId })} optionLabel={(batch) => `${batch.batchNumber} - ${batch.part}`} />
        <SelectField label="Customer (optional)" path="/lookups/customers" required={false} value={form.customerId} onChange={(customerId) => setForm({ ...form, customerId })} />
        <button className="primary-button" type="submit">Link vehicle</button>
      </form>
      {error && <p className="error-message">{error}</p>}
    </Panel>
  )
}

function ImportCsv({ onDone }) {
  const [file, setFile] = useState(null)
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')

  async function send(dryRun) {
    setError('')
    setResult(null)
    try {
      const body = new FormData()
      body.append('file', file)
      const response = await apiClient.post('/admin/vin/import', body, {
        params: { dryRun },
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      setResult(response.data)
      if (!dryRun) onDone()
    } catch (failure) {
      setError(errorMessage(failure))
    }
  }

  return (
    <Panel title="Import a CSV">
      <p className="state-note">Columns: vin, batch_number, serial, customer_code. The first row is the header.</p>
      <div className="inline-form">
        <label>CSV file<input type="file" accept=".csv,text/csv" onChange={(event) => setFile(event.target.files[0] || null)} /></label>
        <button className="text-button" type="button" disabled={!file} onClick={() => send(true)}>Preview</button>
        <button className="primary-button" type="button" disabled={!file} onClick={() => send(false)}>Import</button>
      </div>
      {error && <p className="error-message">{error}</p>}
      {result && (
        <>
          <p className="success-message">{result.valid} valid rows{result.dryRun ? ' found. Nothing has been saved yet.' : ' imported.'}</p>
          {result.errors.map((problem) => <p className="error-message" key={problem}>{problem}</p>)}
        </>
      )}
    </Panel>
  )
}

export function VinPage() {
  const { user } = useAuth()
  const links = useLoad('/admin/vin/links')

  return (
    <>
      <PageHero machine="assembly" eyebrow="Vehicles" title="VIN links" description="Connect vehicles to the production batches they were built from." />
      <div className="page-body">
        {user.role === 'ADMIN' && <LinkVin onDone={links.reload} />}
        {user.role === 'ADMIN' && <ImportCsv onDone={links.reload} />}
        <Panel title="Linked vehicles">
          <LoadState state={links} empty="No vehicles are linked yet.">
            {links.data && <DataTable columns={columns} rows={links.data} />}
          </LoadState>
        </Panel>
      </div>
    </>
  )
}
