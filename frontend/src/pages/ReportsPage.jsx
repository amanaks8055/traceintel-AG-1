import { useLoad } from '../api/useLoad'
import { CountBars } from '../components/CountBars'
import { PageHero } from '../components/PageHero'
import { LoadState } from '../components/ui'

export function ReportsPage() {
  const report = useLoad('/reports/summary')

  return (
    <>
      <PageHero machine="cnc" eyebrow="Plant performance" title="Reports" description="Totals across scans, lots, holds, inspections and shipments." />
      <div className="page-body">
        <LoadState state={report}>
          {report.data && (
            <div className="report-grid">
              <CountBars title="Scan block reasons" counts={report.data.blockReasons} />
              <CountBars title="Lot status" counts={report.data.lotStatus} />
              <CountBars title="Hold decisions" counts={report.data.holdStatus} />
              <CountBars title="Inspection results" counts={report.data.inspectionResults} />
              <CountBars title="Shipments by customer" counts={report.data.shipmentsByCustomer} />
            </div>
          )}
        </LoadState>
      </div>
    </>
  )
}
