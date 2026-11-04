import { useState } from 'react'
import { motion } from 'framer-motion'
import { apiClient, errorMessage } from '../api/client'
import { DemoQr } from '../components/DemoQr'
import { PageHero } from '../components/PageHero'
import { QrScanner } from '../components/QrScanner'
import { SelectField } from '../components/SelectField'
import { Panel, Status } from '../components/ui'

const emptyForm = { batchId: '', station: '', serialInput: '', manualEntry: false, approvedBy: '', approverPassword: '' }

export function ScanPage() {
  const [form, setForm] = useState(emptyForm)
  const [cameraOn, setCameraOn] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  function update(name, value) {
    setForm((current) => ({ ...current, [name]: value }))
  }

  async function submit(event) {
    event.preventDefault()
    setError('')
    setResult(null)
    setIsSubmitting(true)
    try {
      const response = await apiClient.post('/scan/validate', {
        ...form,
        batchId: Number(form.batchId),
        approvedBy: form.manualEntry && form.approvedBy ? Number(form.approvedBy) : null,
        approverPassword: form.manualEntry ? form.approverPassword : null,
      })
      setResult(response.data)
      setForm((current) => ({ ...current, serialInput: '', approverPassword: '' }))
    } catch (failure) {
      setError(errorMessage(failure))
    } finally {
      setIsSubmitting(false)
    }
  }

  function scanned(text) {
    update('serialInput', text)
    setCameraOn(false)
  }

  const blocked = result?.result === 'BLOCKED'

  return (
    <>
      <PageHero machine="conveyor" eyebrow="Production line" title="Scan station" description="Validate a serial before it moves to the next station." />
      <div className="page-body">
        <Panel title="Scan a part">
          <form className="form-grid" onSubmit={submit}>
            <SelectField label="Production batch" path="/batches" value={form.batchId} onChange={(value) => update('batchId', value)} optionLabel={(batch) => `${batch.batchNumber} - ${batch.part}`} />
            <label>Station<input maxLength="50" value={form.station} onChange={(event) => update('station', event.target.value)} /></label>
            <label className="wide">Serial number<input required maxLength="100" value={form.serialInput} onChange={(event) => update('serialInput', event.target.value)} /></label>
            {form.batchId && <DemoQr key={form.batchId} batchId={form.batchId} onScan={scanned} />}
            {cameraOn && <QrScanner onScan={scanned} />}
            <label className="check-field"><input type="checkbox" checked={form.manualEntry} onChange={(event) => update('manualEntry', event.target.checked)} />Manual entry</label>
            {form.manualEntry && <SelectField label="Approving supervisor" path="/scan/approvers" value={form.approvedBy} onChange={(value) => update('approvedBy', value)} />}
            {form.manualEntry && <label>Supervisor password<input required type="password" autoComplete="off" value={form.approverPassword} onChange={(event) => update('approverPassword', event.target.value)} /></label>}
            <div className="row-actions">
              <button className="primary-button" disabled={isSubmitting} type="submit">{isSubmitting ? 'Checking...' : 'Validate scan'}</button>
              <button className="text-button" type="button" onClick={() => setCameraOn(!cameraOn)}>{cameraOn ? 'Stop camera' : 'Scan QR with camera'}</button>
            </div>
          </form>
          {error && <p className="error-message">{error}</p>}
        </Panel>
        {result && (
          <motion.section
            key={result.scanEventId}
            className={blocked ? 'scan-result blocked' : 'scan-result accepted'}
            initial={{ opacity: 0, scale: 0.97 }}
            animate={blocked ? { opacity: 1, scale: 1, x: [0, -9, 9, -5, 5, 0] } : { opacity: 1, scale: 1 }}
            transition={{ duration: 0.4 }}
          >
            <Status value={result.result} />
            <h2>{result.message}</h2>
            <dl>
              <div><dt>Serial</dt><dd>{result.serial}</dd></div>
              <div><dt>Part</dt><dd>{result.partName || '-'}</dd></div>
              <div><dt>Batch</dt><dd>{result.batchNumber}</dd></div>
              {result.reason && <div><dt>Reason</dt><dd>{result.reason}</dd></div>}
            </dl>
          </motion.section>
        )}
      </div>
    </>
  )
}
