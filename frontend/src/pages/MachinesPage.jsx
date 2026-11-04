import { CrudPage } from '../components/CrudPage'
import { Status } from '../components/ui'

const fields = [
  { name: 'code', label: 'Machine ID', required: true },
  { name: 'name', label: 'Name', required: true },
  { name: 'station', label: 'Station' },
  { name: 'location', label: 'Location' },
  { name: 'active', label: 'In service', type: 'checkbox' },
]

const columns = [
  ['Machine ID', (machine) => machine.code],
  ['Name', (machine) => machine.name],
  ['Station', (machine) => machine.station || '-'],
  ['Location', (machine) => machine.location || '-'],
  ['Status', (machine) => <Status value={machine.active ? 'ACTIVE' : 'INACTIVE'} />],
]

export function MachinesPage() {
  return (
    <CrudPage
      hero={{ machine: 'welder', eyebrow: 'Administration', title: 'Machines', description: 'Every machine on the line, including those shown in the plant views.' }}
      path="/admin/machines"
      noun="Machine"
      fields={fields}
      columns={columns}
    />
  )
}
