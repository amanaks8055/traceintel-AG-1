import { CrudPage } from '../components/CrudPage'

const fields = [
  { name: 'code', label: 'Code', required: true },
  { name: 'name', label: 'Name', required: true },
  { name: 'contactEmail', label: 'Contact email', type: 'email' },
  { name: 'phone', label: 'Phone' },
  { name: 'address', label: 'Address' },
]

const columns = [
  ['Code', (contact) => contact.code],
  ['Name', (contact) => contact.name],
  ['Email', (contact) => contact.contactEmail || '-'],
  ['Phone', (contact) => contact.phone || '-'],
  ['Address', (contact) => contact.address || '-'],
]

export function SuppliersPage() {
  return (
    <CrudPage
      hero={{ machine: 'arm', eyebrow: 'Administration', title: 'Suppliers', description: 'Companies that deliver raw material lots.' }}
      path="/admin/suppliers"
      noun="Supplier"
      fields={fields}
      columns={columns}
    />
  )
}

export function CustomersPage() {
  return (
    <CrudPage
      hero={{ machine: 'conveyor', eyebrow: 'Administration', title: 'Customers', description: 'Vehicle makers that receive finished parts.' }}
      path="/admin/customers"
      noun="Customer"
      fields={fields}
      columns={columns}
    />
  )
}
