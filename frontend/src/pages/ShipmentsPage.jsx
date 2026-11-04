import { useState } from 'react'
import { apiClient, errorMessage } from '../api/client'
import { useLoad } from '../api/useLoad'
import { useAuth } from '../auth/useAuth'
import { PageHero } from '../components/PageHero'
import { SelectField } from '../components/SelectField'
import { DataTable, LoadState, Panel, Status } from '../components/ui'

const emptyShipment = { shipmentNumber: '', customerId: '', shippedDate: '', notes: '' }

export function ShipmentsPage() {
  const { user } = useAuth()
  const shipments = useLoad('/shipments')
  const [form, setForm] = useState(emptyShipment)
  const [item, setItem] = useState({ shipmentId: '', batchId: '', quantity: '' })
  const [error, setError] = useState('')
  const canCreate = ['ADMIN', 'SUPERVISOR'].includes(user.role)

  async function run(action) {
    setError('')
    try {
      await action()
      shipments.reload()
    } catch (failure) {
      setError(errorMessage(failure))
    }
  }

  function create(event) {
    event.preventDefault()
    run(async () => {
      await apiClient.post('/shipments', { ...form, customerId: Number(form.customerId) })
      setForm(emptyShipment)
    })
  }

  function addBatch(event) {
    event.preventDefault()
    run(async () => {
      await apiClient.post(`/shipments/${item.shipmentId}/items`, { batchId: Number(item.batchId), quantity: Number(item.quantity) })
      setItem({ shipmentId: '', batchId: '', quantity: '' })
    })
  }

  const columns = [
    ['Shipment', (shipment) => <>{shipment.shipmentNumber} {shipment.demo && <Status value="DEMO" />}</>],
    ['Customer', (shipment) => shipment.customerName],
    ['Shipped', (shipment) => shipment.shippedDate],
    ['Batches', (shipment) => shipment.items.map((entry) => `${entry.batchNumber} (${entry.quantity})`).join(', ') || '-'],
    ['Notes', (shipment) => shipment.notes || '-'],
  ]

  return (
    <>
      <PageHero machine="conveyor" eyebrow="Outbound logistics" title="Shipments" description="Customer shipments and the production batches in each dispatch." />
      <div className="page-body">
        {canCreate && (
          <Panel title="Create a shipment">
            <form className="form-grid" onSubmit={create}>
              <label>Shipment number<input required maxLength="50" value={form.shipmentNumber} onChange={(event) => setForm({ ...form, shipmentNumber: event.target.value })} /></label>
              <SelectField label="Customer" path="/lookups/customers" value={form.customerId} onChange={(customerId) => setForm({ ...form, customerId })} />
              <label>Shipped date<input required type="date" value={form.shippedDate} onChange={(event) => setForm({ ...form, shippedDate: event.target.value })} /></label>
              <label>Notes<input maxLength="500" value={form.notes} onChange={(event) => setForm({ ...form, notes: event.target.value })} /></label>
              <button className="primary-button" type="submit">Create shipment</button>
            </form>
          </Panel>
        )}
        <Panel title="Add a batch to a shipment">
          <form className="form-grid" onSubmit={addBatch}>
            <label>Shipment
              <select required value={item.shipmentId} onChange={(event) => setItem({ ...item, shipmentId: event.target.value })}>
                <option value="">Choose...</option>
                {shipments.data?.map((shipment) => <option key={shipment.id} value={shipment.id}>{shipment.shipmentNumber}</option>)}
              </select>
            </label>
            <SelectField label="Batch" path="/batches" value={item.batchId} onChange={(batchId) => setItem({ ...item, batchId })} optionLabel={(batch) => `${batch.batchNumber} - ${batch.part}`} />
            <label>Quantity<input required type="number" min="1" value={item.quantity} onChange={(event) => setItem({ ...item, quantity: event.target.value })} /></label>
            <button className="primary-button" type="submit">Add batch</button>
          </form>
        </Panel>
        {error && <p className="error-message">{error}</p>}
        <Panel title="All shipments">
          <LoadState state={shipments} empty="No shipments yet.">
            {shipments.data && <DataTable columns={columns} rows={shipments.data} />}
          </LoadState>
        </Panel>
      </div>
    </>
  )
}
