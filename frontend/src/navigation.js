import { BarChart3, Boxes, ClipboardCheck, ClipboardList, Cog, Factory, FileBarChart, FileUp, Layers, Package, ScanLine, Search, Settings, ShieldAlert, Truck, UserCog, Users, Wrench } from 'lucide-react'

const STAFF = ['ADMIN', 'OPERATOR', 'SUPERVISOR', 'QUALITY_ENGINEER', 'MANAGER']
const FLOOR = ['ADMIN', 'OPERATOR', 'SUPERVISOR']
const QUALITY = ['ADMIN', 'SUPERVISOR', 'QUALITY_ENGINEER', 'MANAGER']
const LEADERS = ['ADMIN', 'MANAGER']
const ADMIN = ['ADMIN']

export const navigation = [
  {
    group: 'Production',
    links: [
      { to: '/dashboard', label: 'Dashboard', icon: BarChart3 },
      { to: '/lots', label: 'Material Lots', icon: Package, roles: STAFF },
      { to: '/batches', label: 'Production Batches', icon: Layers, roles: STAFF },
      { to: '/scan', label: 'Scan Station', icon: ScanLine, roles: FLOOR },
      { to: '/shipments', label: 'Shipments', icon: Truck, roles: FLOOR },
    ],
  },
  {
    group: 'Trace & Quality',
    links: [
      { to: '/trace', label: 'Forward Trace', icon: Boxes, roles: QUALITY },
      { to: '/trace/backward', label: 'Backward Trace', icon: Search, roles: QUALITY },
      { to: '/inspections', label: 'Inspections', icon: ClipboardCheck, roles: QUALITY },
      { to: '/holds', label: 'Hold Management', icon: ShieldAlert, roles: QUALITY },
      { to: '/reports', label: 'Reports', icon: FileBarChart, roles: LEADERS },
    ],
  },
  {
    group: 'Administration',
    links: [
      { to: '/admin/parts', label: 'Parts', icon: Cog, roles: ADMIN },
      { to: '/admin/bom', label: 'BOM', icon: Layers, roles: ADMIN },
      { to: '/admin/suppliers', label: 'Suppliers', icon: Factory, roles: ADMIN },
      { to: '/admin/machines', label: 'Machines', icon: Wrench, roles: ADMIN },
      { to: '/admin/customers', label: 'Customers', icon: Users, roles: ADMIN },
      { to: '/admin/users', label: 'Users', icon: UserCog, roles: ADMIN },
      { to: '/admin/vin-import', label: 'VIN Links', icon: FileUp, roles: LEADERS },
      { to: '/audit', label: 'Audit Log', icon: ClipboardList, roles: LEADERS },
    ],
  },
  {
    group: 'Account',
    links: [{ to: '/settings', label: 'Settings', icon: Settings }],
  },
]

export function canOpen(link, role) {
  return !link.roles || link.roles.includes(role)
}
