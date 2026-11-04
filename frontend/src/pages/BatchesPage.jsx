import { useState } from 'react'
import { apiClient, errorMessage } from '../api/client'
import { useLoad } from '../api/useLoad'
import { useAuth } from '../auth/useAuth'
import { PageHero } from '../components/PageHero'
import { SelectField } from '../components/SelectField'
import { DataTable, LoadState, Panel, Status } from '../components/ui'

const emptyBatch = { batchNumber: '', partId: '', machineId: '', operatorId: '', shift: 'DAY', quantity: '' }

function lotLabel(lot) {
  return `${lot.lotNumber} - ${lot.part || 'no material'} (${lot.expired ? 'EXPIRED' : lot.status}, ${lot.remaining} ${lot.unit} left)`
}

function materialHint(lot, batch) {
  if (!lot.part) return { blocked: true, text: `${lot.lotNumber} has no material, so it cannot be used.` }
  const use = lot.canMake.find((item) => item.partId === batch.partId)
  if (!use && lot.part === batch.part) {
    return { blocked: true, text: `${lot.lotNumber} has ${batch.part} itself as its material. Choose a lot of the raw material ${batch.part} is made from.` }
  }
  if (!use && !batch.hasBom) {
    return { blocked: true, text: `${batch.part} has no bill of materials yet. An admin must add its materials on the Bill of materials page first.` }
  }
  if (!use) return { blocked: true, text: `${lot.part} is not in the bill of materials for ${batch.part}.` }
  const needed = Number((use.perPiece * batch.quantity).toFixed(3))
  const enough = lot.remaining >= needed
  return {
    blocked: false,
    fill: String(enough ? needed : lot.remaining),
    text: `1 piece needs ${use.perPiece} ${lot.unit}. This batch of ${batch.quantity} needs ${needed} ${lot.unit}. `
      + `${lot.remaining} ${lot.unit} is left in the lot, enough for ${use.pieces} pieces.`
      + (enough ? '' : ' Link the rest from another lot.'),
  }
}

function BatchDetail({ batch, canManage, canLink, onChange }) {
  const serials = useLoad(`/batches/${batch.id}/serials`)
  const lots = useLoad('/lots')
  const [count, setCount] = useState('5')
  const [lotId, setLotId] = useState('')
  const [used, setUsed] = useState('')
  const [qr, setQr] = useState(null)
  const [error, setError] = useState('')
  const lot = lots.data?.find((item) => String(item.id) === lotId)
  const hint = lot && materialHint(lot, batch)

  function chooseLot(value) {
    setLotId(value)
    const chosen = lots.data?.find((item) => String(item.id) === value)
    setUsed(chosen ? materialHint(chosen, batch).fill || '' : '')
  }

  async function run(action) {
    setError('')
    try {
      await action()
      serials.reload()
      lots.reload()
      onChange()
    } catch (failure) {
      setError(errorMessage(failure))
    }
  }

  async function showQr(serial) {
    setError('')
    try {
      const response = await apiClient.get(`/qr/${encodeURIComponent(serial.serial)}`, { responseType: 'blob' })
      setQr({ serial: serial.serial, url: URL.createObjectURL(response.data) })
    } catch (failure) {
      setError(errorMessage(failure))
    }
  }

  const columns = [
    ['Serial', (serial) => serial.serial],
    ['Part', (serial) => serial.part],
    ['QR', (serial) => <button className="text-button" type="button" onClick={() => showQr(serial)}>Show QR</button>],
  ]

  return (
    <Panel title={`${batch.batchNumber} details`}>
      <div className="form-grid">
        {canLink && (
          <form className="inline-form wide" onSubmit={(event) => {
            event.preventDefault()
            run(() => apiClient.post('/scan/material-lot', { batchId: batch.id, lotId: Number(lotId), quantity: Number(used) }))
          }}>
            <label>
              Material lot used
              <select required value={lotId} onChange={(event) => chooseLot(event.target.value)}>
                <option value="">{lots.loading ? 'Loading...' : lots.error ? 'Could not load' : 'Choose...'}</option>
                {lots.data?.map((item) => <option key={item.id} value={item.id}>{lotLabel(item)}</option>)}
              </select>
            </label>
            <label>Material used<input required type="number" min="0.01" step="0.01" value={used} onChange={(event) => setUsed(event.target.value)} /></label>
            <button className="primary-button" disabled={hint?.blocked} type="submit">Link lot</button>
          </form>
        )}
        {canLink && hint && <p className={hint.blocked ? 'error-message wide' : 'wide'}>{hint.text}</p>}
        {canManage && (
          <form className="inline-form wide" onSubmit={(event) => {
            event.preventDefault()
            run(() => apiClient.post(`/batches/${batch.id}/serials`, { count: Number(count) }))
          }}>
            <label>Serials to generate<input required type="number" min="1" max="500" value={count} onChange={(event) => setCount(event.target.value)} /></label>
            <button className="primary-button" type="submit">Generate serials</button>
          </form>
        )}
      </div>
      {error && <p className="error-message">{error}</p>}
      {qr && (
        <figure className="qr-preview">
          <img src={qr.url} alt={`QR code for ${qr.serial}`} />
          <figcaption>{qr.serial}</figcaption>
        </figure>
      )}
      <LoadState state={serials} empty="No serials generated yet.">
        {serials.data && <DataTable columns={columns} rows={serials.data} />}
      </LoadState>
    </Panel>
  )
}

export function BatchesPage() {
  const { user } = useAuth()
  const batches = useLoad('/batches')
  const [form, setForm] = useState(emptyBatch)
  const [selectedId, setSelectedId] = useState(null)
  const [error, setError] = useState('')
  const canManage = ['ADMIN', 'SUPERVISOR'].includes(user.role)
  const canLink = ['ADMIN', 'SUPERVISOR', 'OPERATOR'].includes(user.role)
  const selected = batches.data?.find((batch) => batch.id === selectedId)

  async function start(event) {
    event.preventDefault()
    setError('')
    try {
      const { partId, machineId, operatorId, quantity } = form
      await apiClient.post('/batches', { ...form, partId: Number(partId), machineId: Number(machineId), operatorId: Number(operatorId), quantity: Number(quantity) })
      setForm(emptyBatch)
      batches.reload()
    } catch (failure) {
      setError(errorMessage(failure))
    }
  }

  const columns = [
    ['Batch', (batch) => <>{batch.batchNumber} {batch.demo && <Status value="DEMO" />}</>],
    ['Part', (batch) => batch.part],
    ['Machine', (batch) => batch.machine],
    ['Operator', (batch) => batch.operator],
    ['Quantity', (batch) => batch.quantity],
    ['Lots', (batch) => batch.lots.join(', ') || '-'],
    ['Serials', (batch) => batch.serialCount],
    ['Action', (batch) => <button className="text-button" type="button" onClick={() => setSelectedId(batch.id)}>Open</button>],
  ]

  return (
    <>
      <PageHero machine="cnc" eyebrow="Production" title="Production batches" description="Each batch records its part, machine, operator, material lots and serials." />
      <div className="page-body">
        {canManage && (
          <Panel title="Start a batch">
            <form className="form-grid" onSubmit={start}>
              <label>Batch number<input required maxLength="50" value={form.batchNumber} onChange={(event) => setForm({ ...form, batchNumber: event.target.value })} /></label>
              <SelectField label="Part" path="/lookups/parts" value={form.partId} onChange={(partId) => setForm({ ...form, partId })} />
              <SelectField label="Machine" path="/lookups/machines" value={form.machineId} onChange={(machineId) => setForm({ ...form, machineId })} />
              <SelectField label="Operator" path="/lookups/operators" value={form.operatorId} onChange={(operatorId) => setForm({ ...form, operatorId })} />
              <label>Shift
                <select value={form.shift} onChange={(event) => setForm({ ...form, shift: event.target.value })}>
                  {['DAY', 'NIGHT', 'GENERAL'].map((shift) => <option key={shift}>{shift}</option>)}
                </select>
              </label>
              <label>Quantity<input required type="number" min="1" value={form.quantity} onChange={(event) => setForm({ ...form, quantity: event.target.value })} /></label>
              <button className="primary-button" type="submit">Start batch</button>
            </form>
            {error && <p className="error-message">{error}</p>}
          </Panel>
        )}
        {selected && <BatchDetail key={selected.id} batch={selected} canManage={canManage} canLink={canLink} onChange={batches.reload} />}
        <Panel title="All batches">
          <LoadState state={batches} empty="No batches yet.">
            {batches.data && <DataTable columns={columns} rows={batches.data} />}
          </LoadState>
        </Panel>
      </div>
    </>
  )
}
