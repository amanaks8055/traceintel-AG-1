import { useEffect, useState } from 'react'
import { animate, motion } from 'framer-motion'
import { useLoad } from '../api/useLoad'
import { CountBars } from '../components/CountBars'
import { prefersReducedMotion } from '../components/MachineStage'
import { PageHero } from '../components/PageHero'
import { DataTable, formatDate, LoadState, Panel, Status } from '../components/ui'

const scanColumns = [
  ['Serial', (scan) => scan.serialInput],
  ['Batch', (scan) => scan.batchNumber],
  ['Operator', (scan) => scan.operator],
  ['Result', (scan) => <Status value={scan.result} />],
  ['Reason', (scan) => scan.reason || '-'],
  ['Time', (scan) => formatDate(scan.scannedAt)],
]

function Counter({ value }) {
  const [shown, setShown] = useState(0)

  useEffect(() => {
    const controls = animate(0, value, {
      duration: prefersReducedMotion() ? 0 : 0.9,
      onUpdate: (current) => setShown(Math.round(current)),
    })
    return () => controls.stop()
  }, [value])

  return shown.toLocaleString()
}

function ActivityChart({ days }) {
  const busiest = Math.max(1, ...days.map((day) => day.accepted + day.blocked))
  return (
    <div className="activity-chart">
      {days.map((day) => (
        <div className="activity-day" key={day.date} title={`${day.accepted} accepted, ${day.blocked} blocked`}>
          <strong>{day.accepted + day.blocked}</strong>
          <div className="activity-track">
            <motion.div className="activity-blocked" initial={{ height: 0 }} animate={{ height: `${(day.blocked / busiest) * 100}%` }} />
            <motion.div className="activity-accepted" initial={{ height: 0 }} animate={{ height: `${(day.accepted / busiest) * 100}%` }} />
          </div>
          <span>{new Date(day.date).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' })}</span>
        </div>
      ))}
    </div>
  )
}

export function DashboardPage() {
  const stats = useLoad('/dashboard/stats')
  const activity = useLoad('/dashboard/scan-activity')
  const recent = useLoad('/dashboard/recent-scans')
  const reasons = useLoad('/dashboard/block-reasons')

  return (
    <>
      <PageHero machine="assembly" eyebrow="Operations overview" title="Plant dashboard" description="Live production, scan and quality figures from the plant database." />
      <div className="page-body">
        <LoadState state={stats}>
          {stats.data && (
            <div className="stats-grid">
              {[['Production batches', stats.data.totalBatches], ['Scans today', stats.data.scansToday], ['Blocked today', stats.data.blockedToday], ['Lots on hold', stats.data.lotsOnHold], ['Pending holds', stats.data.pendingHolds]].map(([label, value]) => (
                <motion.section className="panel" key={label} initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }}>
                  <span className="stat-label">{label}</span>
                  <strong className="stat-value"><Counter value={value} /></strong>
                </motion.section>
              ))}
            </div>
          )}
        </LoadState>
        <Panel title="Scan activity, last 7 days">
          <LoadState state={activity} empty="No scan activity yet.">
            {activity.data && <ActivityChart days={activity.data} />}
          </LoadState>
        </Panel>
        {reasons.data && <CountBars title="Why scans were blocked" counts={reasons.data} />}
        <Panel title="Recent scans">
          <LoadState state={recent} empty="No scans recorded yet.">
            {recent.data && <DataTable columns={scanColumns} rows={recent.data} />}
          </LoadState>
        </Panel>
      </div>
    </>
  )
}
