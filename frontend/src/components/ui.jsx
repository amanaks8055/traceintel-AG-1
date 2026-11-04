import { motion } from 'framer-motion'

export function Panel({ title, children }) {
  return (
    <motion.section
      className="panel"
      initial={{ opacity: 0, y: 22 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.1 }}
      transition={{ duration: 0.4 }}
    >
      {title && <h2>{title}</h2>}
      {children}
    </motion.section>
  )
}

export function LoadState({ state, empty, children }) {
  if (state.loading) return <p className="state-note">Loading...</p>
  if (state.error) return <p className="error-message">{state.error}</p>
  if (Array.isArray(state.data) && state.data.length === 0) return <p className="state-note">{empty}</p>
  return children
}

export function DataTable({ columns, rows }) {
  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>{columns.map(([label]) => <th key={label}>{label}</th>)}</tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.id}>
              {columns.map(([label, read]) => <td key={label}>{read(row)}</td>)}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export function Status({ value }) {
  return <span className={`status status-${String(value).toLowerCase()}`}>{value}</span>
}

export function formatDate(value) {
  return value ? new Date(value).toLocaleString('en-GB', { dateStyle: 'medium', timeStyle: 'short' }) : '-'
}
