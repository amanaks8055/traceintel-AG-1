import { motion } from 'framer-motion'
import { Panel } from './ui'

export function CountBars({ title, counts }) {
  const largest = Math.max(1, ...counts.map((count) => count.total))
  return (
    <Panel title={title}>
      {counts.length === 0 ? <p className="state-note">No data yet.</p> : counts.map((count) => (
        <div className="report-bar" key={count.label}>
          <span>{count.label.replaceAll('_', ' ')}</span>
          <div><motion.i initial={{ width: 0 }} animate={{ width: `${(count.total / largest) * 100}%` }} /></div>
          <strong>{count.total}</strong>
        </div>
      ))}
    </Panel>
  )
}