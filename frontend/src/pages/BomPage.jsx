import { useState } from 'react'
import { apiClient, errorMessage } from '../api/client'
import { useLoad } from '../api/useLoad'
import { PageHero } from '../components/PageHero'
import { SelectField } from '../components/SelectField'
import { DataTable, LoadState, Panel } from '../components/ui'

function BomList({ partId }) {
  const items = useLoad(`/admin/bom/${partId}`)
  const parts = useLoad('/lookups/parts')
  const others = parts.data?.filter((part) => String(part.id) !== partId) || []
  const rawMaterials = others.filter((part) => part.label.startsWith('RAW-'))
  const otherParts = others.filter((part) => !part.label.startsWith('RAW-'))
  const [childPartId, setChildPartId] = useState('')
  const [quantity, setQuantity] = useState('1')
  const [error, setError] = useState('')

  async function add(event) {
    event.preventDefault()
    setError('')
    try {
      await apiClient.post('/admin/bom', { parentPartId: Number(partId), childPartId: Number(childPartId), quantity: Number(quantity) })
      setChildPartId('')
      items.reload()
    } catch (failure) {
      setError(errorMessage(failure))
    }
  }

  async function remove(item) {
    setError('')
    try {
      await apiClient.delete(`/admin/bom/${item.id}`)
      items.reload()
    } catch (failure) {
      setError(errorMessage(failure))
    }
  }

  const columns = [
    ['Part number', (item) => item.partNumber],
    ['Component', (item) => item.name],
    ['Quantity', (item) => item.quantity],
    ['Action', (item) => <button className="text-button" type="button" onClick={() => remove(item)}>Remove</button>],
  ]

  return (
    <>
      <Panel title="Add component">
        <form className="form-grid" onSubmit={add}>
          <label>
            Component
            <select required value={childPartId} onChange={(event) => setChildPartId(event.target.value)}>
              <option value="">{parts.loading ? 'Loading...' : parts.error ? 'Could not load' : 'Choose...'}</option>
              <optgroup label="Raw materials">
                {rawMaterials.map((part) => <option key={part.id} value={part.id}>{part.label}</option>)}
              </optgroup>
              <optgroup label="Other parts (sub-assemblies)">
                {otherParts.map((part) => <option key={part.id} value={part.id}>{part.label}</option>)}
              </optgroup>
            </select>
          </label>
          <label>Quantity<input required type="number" min="0.001" step="any" value={quantity} onChange={(event) => setQuantity(event.target.value)} /></label>
          <button className="primary-button" type="submit">Add component</button>
        </form>
        {error && <p className="error-message">{error}</p>}
      </Panel>
      <Panel title="Components">
        <LoadState state={items} empty="This part has no components yet.">
          {items.data && <DataTable columns={columns} rows={items.data} />}
        </LoadState>
      </Panel>
    </>
  )
}

export function BomPage() {
  const [partId, setPartId] = useState('')

  return (
    <>
      <PageHero machine="assembly" eyebrow="Administration" title="Bill of materials" description="The components each part is made from. Scans are checked against this list." />
      <div className="page-body">
        <Panel title="Choose a part">
          <SelectField label="Part" path="/lookups/parts" value={partId} onChange={setPartId} />
        </Panel>
        {partId && <BomList key={partId} partId={partId} />}
      </div>
    </>
  )
}
