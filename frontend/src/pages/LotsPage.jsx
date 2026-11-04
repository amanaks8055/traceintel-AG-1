import { useAuth } from '../auth/useAuth'
import { CrudPage } from '../components/CrudPage'
import { Status } from '../components/ui'

const fields = [
  { name: 'lotNumber', label: 'Lot number', required: true },
  { name: 'supplierId', label: 'Supplier', options: '/lookups/suppliers', required: true },
  {
    name: 'partId',
    label: 'Material',
    options: '/lookups/parts',
    required: true,
    groups: [['Raw materials', (part) => part.label.startsWith('RAW-')]],
  },
  { name: 'receivedDate', label: 'Received', type: 'date', required: true },
  { name: 'expiryDate', label: 'Expiry', type: 'date' },
  { name: 'quantity', label: 'Quantity', type: 'number', required: true },
  { name: 'unit', label: 'Unit' },
]

const columns = [
  ['Lot', (lot) => <>{lot.lotNumber} {lot.demo && <Status value="DEMO" />}</>],
  ['Supplier', (lot) => lot.supplier],
  ['Material', (lot) => lot.part || '-'],
  ['Received', (lot) => lot.receivedDate],
  ['Expiry', (lot) => lot.expiryDate || '-'],
  ['Quantity', (lot) => `${lot.quantity} ${lot.unit}`],
  ['Left', (lot) => `${lot.remaining} ${lot.unit}`],
  ['Can make', (lot) => {
    if (lot.status !== 'OK' || lot.expired || !lot.canMake.length) return '-'
    return lot.canMake.map((item) => <div key={item.partId}>{item.part}: {item.pieces}</div>)
  }],
  ['Status', (lot) => (
    <div className="row-actions">
      <Status value={lot.status} />
      {lot.expired && <Status value="EXPIRED" />}
    </div>
  )],
]

export function LotsPage() {
  const { user } = useAuth()
  return (
    <CrudPage
      hero={{ machine: 'conveyor', eyebrow: 'Receiving', title: 'Material lots', description: 'Raw material received from suppliers, and its current status.' }}
      path="/lots"
      noun="Lot"
      fields={fields}
      columns={columns}
      canEdit={false}
      canCreate={['ADMIN', 'SUPERVISOR'].includes(user.role)}
    />
  )
}
