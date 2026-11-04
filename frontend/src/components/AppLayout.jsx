import { useState } from 'react'
import { NavLink } from 'react-router-dom'
import { Factory, LogOut, Menu, X } from 'lucide-react'
import { useAuth } from '../auth/useAuth'
import { canOpen, navigation } from '../navigation'

export function AppLayout({ children }) {
  const { user, signOut } = useAuth()
  const [menuOpen, setMenuOpen] = useState(false)

  return (
    <div className="app-shell">
      <button className="menu-button" type="button" aria-label="Toggle menu" onClick={() => setMenuOpen(!menuOpen)}>
        {menuOpen ? <X size={20} /> : <Menu size={20} />}
      </button>
      <aside className={menuOpen ? 'sidebar open' : 'sidebar'}>
        <NavLink className="brand" to="/">
          <span className="brand-mark"><Factory size={17} /></span>
          TraceIntel
        </NavLink>
        <nav>
          {navigation.map(({ group, links }) => {
            const visible = links.filter((link) => canOpen(link, user.role))
            if (visible.length === 0) return null
            return (
              <div key={group}>
                <p className="nav-label">{group}</p>
                {visible.map(({ to, label, icon: Icon }) => (
                  <NavLink className="nav-link" key={to} to={to} end onClick={() => setMenuOpen(false)}>
                    <Icon size={16} /> {label}
                  </NavLink>
                ))}
              </div>
            )
          })}
        </nav>
        <div className="sidebar-footer">
          <strong>{user.fullName}</strong>
          <span>{user.role.replace('_', ' ')}</span>
          <button className="logout-button" onClick={signOut} type="button">
            <LogOut size={15} /> Sign out
          </button>
        </div>
      </aside>
      <main className="main-content">{children}</main>
    </div>
  )
}
