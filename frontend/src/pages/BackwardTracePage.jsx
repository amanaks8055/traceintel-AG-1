import { useState } from 'react'
import { apiClient, errorMessage } from '../api/client'
import { PageHero } from '../components/PageHero'
import { Panel, Status } from '../components/ui'

export function BackwardTracePage() {
  const [input, setInput] = useState('')
  const [trace, setTrace] = useState(null)
  const [error, setError] = useState('')
  const [isRunning, setIsRunning] = useState(false)

  async function run(event) {
    event.preventDefault()
    setError('')
    setTrace(null)
    setIsRunning(true)
    try {
      const response = await apiClient.post('/trace/backward', { input: input.trim() })
      setTrace(response.data)
    } catch (failure) {
      setError(errorMessage(failure))
    } finally {
      setIsRunning(false)
    }
  }

  const chain = trace && [
    ['VIN', trace.vin],
    ['Customer', trace.customer],
    ['Serial', trace.serial],
    ['Batch', trace.batches.join(', ')],
    ['Part', trace.part],
  ].filter(([, value]) => value)

  return (
    <>
      <PageHero machine="cnc" eyebrow="Quality intelligence" title="Backward trace" description="Start from a VIN or serial and find its batch, lots and suppliers." />
      <div className="page-body">
        <Panel title="Trace a vehicle or part">
          <form className="inline-form" onSubmit={run}>
            <label>VIN or serial<input required maxLength="100" value={input} onChange={(event) => setInput(event.target.value)} /></label>
            <button className="primary-button" disabled={isRunning} type="submit">{isRunning ? 'Tracing...' : 'Trace backward'}</button>
          </form>
          {error && <p className="error-message">{error}</p>}
        </Panel>
        {trace && (
          <Panel title={`Traced in ${trace.durationMs} ms`}>
            <ol className="chain">
              {chain.map(([label, value]) => <li key={label}><span>{label}</span><strong>{value}</strong></li>)}
              {trace.lots.map((lot) => (
                <li key={lot.lotNumber}>
                  <span>Material lot</span>
                  <strong>{lot.lotNumber}, {lot.supplier} <Status value={lot.status} /></strong>
                </li>
              ))}
            </ol>
          </Panel>
        )}
      </div>
    </>
  )
}
