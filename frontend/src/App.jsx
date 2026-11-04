import { AnimatePresence, motion } from 'framer-motion'
import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { useAuth } from './auth/useAuth'
import { AppLayout } from './components/AppLayout'
import { canOpen, navigation } from './navigation'
import { AuditPage } from './pages/AuditPage'
import { BackwardTracePage } from './pages/BackwardTracePage'
import { BatchesPage } from './pages/BatchesPage'
import { BomPage } from './pages/BomPage'
import { CustomersPage, SuppliersPage } from './pages/ContactsPage'
import { DashboardPage } from './pages/DashboardPage'
import { ForwardTracePage } from './pages/ForwardTracePage'
import { HoldsPage } from './pages/HoldsPage'
import { HomePage } from './pages/HomePage'
import { InspectionsPage } from './pages/InspectionsPage'
import { LoginPage } from './pages/LoginPage'
import { LotsPage } from './pages/LotsPage'
import { MachinesPage } from './pages/MachinesPage'
import { PartsPage } from './pages/PartsPage'
import { ReportsPage } from './pages/ReportsPage'
import { ScanPage } from './pages/ScanPage'
import { SettingsPage } from './pages/SettingsPage'
import { ShipmentsPage } from './pages/ShipmentsPage'
import { SignUpPage } from './pages/SignUpPage'
import { ForgotPasswordPage } from './pages/ForgotPasswordPage'
import { UsersPage } from './pages/UsersPage'
import { VinPage } from './pages/VinPage'
import './App.css'

const pages = {
  '/dashboard': DashboardPage,
  '/scan': ScanPage,
  '/shipments': ShipmentsPage,
  '/trace': ForwardTracePage,
  '/trace/backward': BackwardTracePage,
  '/holds': HoldsPage,
  '/admin/parts': PartsPage,
  '/admin/bom': BomPage,
  '/admin/suppliers': SuppliersPage,
  '/admin/machines': MachinesPage,
  '/admin/customers': CustomersPage,
  '/admin/users': UsersPage,
  '/admin/vin-import': VinPage,
  '/audit': AuditPage,
  '/lots': LotsPage,
  '/batches': BatchesPage,
  '/inspections': InspectionsPage,
  '/reports': ReportsPage,
  '/settings': SettingsPage,
}

const links = navigation.flatMap((section) => section.links)

function ProtectedRoutes() {
  const { user } = useAuth()
  const location = useLocation()

  if (!user) {
    return <Navigate to="/login" replace />
  }

  return (
    <AppLayout>
      <AnimatePresence mode="wait">
        <motion.div key={location.pathname} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0 }} transition={{ duration: 0.2 }}>
          <Routes location={location}>
            {links.filter((link) => canOpen(link, user.role)).map((link) => {
              const Page = pages[link.to]
              return <Route key={link.to} path={link.to} element={<Page />} />
            })}
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </motion.div>
      </AnimatePresence>
    </AppLayout>
  )
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignUpPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/*" element={<ProtectedRoutes />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default App
