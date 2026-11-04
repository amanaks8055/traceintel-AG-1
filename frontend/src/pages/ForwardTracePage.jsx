import { useState } from 'react'
import { apiClient, errorMessage } from '../api/client'
import { useLoad } from '../api/useLoad'
import { useAuth } from '../auth/useAuth'
import { PageHero } from '../components/PageHero'
import { SelectField } from '../components/SelectField'
import { DataTable, formatDate, LoadState, Panel, Status } from '../components/ui'

const historyColumns = [
  ['Direction', (run) => run.direction],
  ['Input', (run) => run.inputValue],
  ['Time taken', (run) => `${run.durationMs} ms`],
  ['Run by', (run) => run.executedBy],
  ['When', (run) => formatDate(run.executedAt)],
]

const batchColumns = [
  ['Batch', (batch) => batch.batchNumber],
  ['Part', (batch) => batch.part],
  ['Quantity', (batch) => batch.quantity],
  ['Location', (batch) => <Status value={batch.shipped ? 'SHIPPED' : 'IN_FACTORY'} />],
]

const shipmentColumns = [
  ['Shipment', (shipment) => shipment.shipmentNumber],
  ['Customer', (shipment) => shipment.customer],
  ['Shipped', (shipment) => shipment.shippedDate],
]

function money(value) {
  return `USD ${Number(value).toLocaleString()}`
}

function TraceHistory() {
  const history = useLoad('/trace/history')
  return (
    <Panel title="Trace history">
      <LoadState state={history} empty="No traces have been run yet.">
        {history.data && <DataTable columns={historyColumns} rows={history.data} />}
      </LoadState>
    </Panel>
  )
}

export function ForwardTracePage() {
  const { user } = useAuth()
  const [lotId, setLotId] = useState('')
  const [trace, setTrace] = useState(null)
  const [runs, setRuns] = useState(0)
  const [error, setError] = useState('')
  const [isRunning, setIsRunning] = useState(false)

  async function run(event) {
    event.preventDefault()
    setError('')
    setTrace(null)
    setIsRunning(true)
    try {
      const response = await apiClient.post('/trace/forward', { lotId: Number(lotId) })
      setTrace(response.data)
      setRuns(runs + 1)
    } catch (failure) {
      setError(errorMessage(failure))
    } finally {
      setIsRunning(false)
    }
  }

  return (
    <>
      <PageHero machine="arm" eyebrow="Quality intelligence" title="Forward trace" description="Follow a material lot through batches, shipments, customers and VINs." />
      <div className="page-body">
        <Panel title="Trace a material lot">
          <form className="inline-form" onSubmit={run}>
            <SelectField label="Material lot" path="/lots" value={lotId} onChange={setLotId} optionLabel={(lot) => `${lot.lotNumber} (${lot.status})`} />
            <button className="primary-button" disabled={isRunning} type="submit">{isRunning ? 'Tracing...' : 'Run trace'}</button>
          </form>
          {error && <p className="error-message">{error}</p>}
        </Panel>
        {trace && (
          <>
            <Panel title={`${trace.lotNumber} from ${trace.supplier}, traced in ${trace.durationMs} ms`}>
              {trace.batches.length === 0
                ? <p className="state-note">This lot has not been used in any batch.</p>
                : <DataTable columns={batchColumns} rows={trace.batches} />}
            </Panel>
            <Panel title="Shipments and customers">
              {trace.shipments.length === 0
                ? <p className="state-note">No affected batch has shipped.</p>
                : <DataTable columns={shipmentColumns} rows={trace.shipments.map((shipment) => ({ ...shipment, id: shipment.shipmentNumber }))} />}
            </Panel>
            <div className="trace-grid">
              <Panel title={`Affected VINs (${trace.vins.length})`}>
                {trace.vins.length === 0 ? <p className="state-note">No vehicles are linked.</p> : <ul className="plain-list">{trace.vins.map((vin) => <li key={vin}>{vin}</li>)}</ul>}
              </Panel>
              <Panel title="Estimates">
                <ul className="plain-list">
                  <li>Affected parts: {trace.affectedParts}</li>
                  <li>Estimated vehicles: {trace.estimatedVehicles}</li>
                  <li>Parts cost: {money(trace.partsCost)}</li>
                  <li>Scrap cost: {money(trace.scrapCost)}</li>
                  <li>Rework cost: {money(trace.reworkCost)}</li>
                  <li><strong>Estimated loss: {money(trace.estimatedLoss)}</strong></li>
                </ul>
                <p className="state-note">Estimates, not certain predictions.</p>
              </Panel>
            </div>
          </>
        )}
        {user.role !== 'SUPERVISOR' && <TraceHistory key={runs} />}
      </div>
    </>
  )
}
