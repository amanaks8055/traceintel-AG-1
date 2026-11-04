import { useState } from 'react'
import { useLoad } from '../api/useLoad'
import { PageHero } from '../components/PageHero'
import { DataTable, formatDate, LoadState, Panel } from '../components/ui'

const columns = [
  ['When', (log) => formatDate(log.createdAt)],
  ['User', (log) => log.user || '-'],
  ['Action', (log) => log.action],
  ['Record', (log) => (log.entityType ? `${log.entityType} ${log.entityId ?? ''}` : '-')],
  ['Details', (log) => log.details || '-'],
]

export function AuditPage() {
  const [page, setPage] = useState(0)
  const logs = useLoad(`/audit/logs?page=${page}`)

  return (
    <>
      <PageHero machine="arm" eyebrow="Governance" title="Audit log" description="Who did what, and when." />
      <div className="page-body">
        <Panel title="Activity">
          <LoadState state={logs}>
            {logs.data && (
              <>
                <DataTable columns={columns} rows={logs.data.rows} />
                <div className="row-actions pager">
                  <button className="text-button" type="button" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button>
                  <span>Page {logs.data.page + 1} of {Math.max(1, logs.data.totalPages)}</span>
                  <button className="text-button" type="button" disabled={page + 1 >= logs.data.totalPages} onClick={() => setPage(page + 1)}>Next</button>
                </div>
              </>
            )}
          </LoadState>
        </Panel>
      </div>
    </>
  )
}
