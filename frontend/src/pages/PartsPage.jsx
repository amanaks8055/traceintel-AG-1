import { CrudPage } from '../components/CrudPage'

const fields = [
  { name: 'partNumber', label: 'Part number', required: true },
  { name: 'name', label: 'Name', required: true },
  { name: 'description', label: 'Description' },
  { name: 'costPerPart', label: 'Cost per part', type: 'number' },
  { name: 'partsPerVehicle', label: 'Parts per vehicle', type: 'number' },
  { name: 'scrapCost', label: 'Scrap cost', type: 'number' },
  { name: 'reworkCost', label: 'Rework cost', type: 'number' },
]

const columns = [
  ['Part number', (part) => part.partNumber],
  ['Name', (part) => part.name],
  ['Cost', (part) => part.costPerPart],
  ['Per vehicle', (part) => part.partsPerVehicle],
  ['Scrap', (part) => part.scrapCost],
  ['Rework', (part) => part.reworkCost],
]

export function PartsPage() {
  return (
    <CrudPage
      hero={{ machine: 'cnc', eyebrow: 'Administration', title: 'Parts', description: 'The parts and raw materials used across production.' }}
      path="/admin/parts"
      noun="Part"
      fields={fields}
      columns={columns}
      canDelete
    />
  )
}
