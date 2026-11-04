import { useState } from 'react'
import { apiClient, errorMessage } from '../api/client'
import { useLoad } from '../api/useLoad'
import { PageHero } from './PageHero'
import { SelectField } from './SelectField'
import { DataTable, LoadState, Panel } from './ui'

function emptyForm(fields) {
  return Object.fromEntries(fields.map((field) => [field.name, field.type === 'checkbox' ? true : '']))
}

function toPayload(form, fields) {
  return Object.fromEntries(fields.map(({ name, type }) => {
    const value = form[name]
    if (type === 'checkbox') return [name, value]
    if (value === '') return [name, null]
    return [name, type === 'number' ? Number(value) : value]
  }))
}

export function CrudPage({ hero, path, fields, columns, noun, canDelete = false, canEdit = true, canCreate = true, listPath = path }) {
  const records = useLoad(listPath)
  const [form, setForm] = useState(() => emptyForm(fields))
  const [editingId, setEditingId] = useState(null)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  function reset() {
    setForm(emptyForm(fields))
    setEditingId(null)
  }

  function edit(record) {
    setEditingId(record.id)
    setError('')
    setMessage('')
    setForm(Object.fromEntries(fields.map(({ name }) => [name, record[name] ?? ''])))
  }

  async function save(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      const payload = toPayload(form, fields)
      if (editingId) {
        await apiClient.put(`${path}/${editingId}`, payload)
      } else {
        await apiClient.post(path, payload)
      }
      setMessage(`${noun} saved.`)
      reset()
      records.reload()
    } catch (failure) {
      setError(errorMessage(failure))
    }
  }

  async function remove(record) {
    setError('')
    setMessage('')
    try {
      await apiClient.delete(`${path}/${record.id}`)
      setMessage(`${noun} deleted.`)
      records.reload()
    } catch (failure) {
      setError(errorMessage(failure))
    }
  }

  const actions = canEdit || canDelete ? [['Action', (record) => (
    <div className="row-actions">
      {canEdit && <button className="text-button" type="button" onClick={() => edit(record)}>Edit</button>}
      {canDelete && <button className="text-button" type="button" onClick={() => remove(record)}>Delete</button>}
    </div>
  )]] : []

  return (
    <>
      <PageHero {...hero} />
      <div className="page-body">
        {(canCreate || editingId) && (
          <Panel title={editingId ? `Edit ${noun.toLowerCase()}` : `Add ${noun.toLowerCase()}`}>
            <form className="form-grid" onSubmit={save}>
              {fields.map((field) => (field.options ? (
                <SelectField
                  key={field.name}
                  label={field.label}
                  path={field.options}
                  required={field.required}
                  groups={field.groups}
                  value={form[field.name]}
                  onChange={(value) => setForm({ ...form, [field.name]: value })}
                />
              ) : field.choices ? (
                <label key={field.name}>
                  {field.label}
                  <select required={field.required} value={form[field.name]} onChange={(event) => setForm({ ...form, [field.name]: event.target.value })}>
                    <option value="">Choose...</option>
                    {field.choices.map((choice) => <option key={choice} value={choice}>{choice.replace('_', ' ')}</option>)}
                  </select>
                </label>
              ) : field.type === 'checkbox' ? (
                <label className="check-field" key={field.name}>
                  <input type="checkbox" checked={form[field.name]} onChange={(event) => setForm({ ...form, [field.name]: event.target.checked })} />
                  {field.label}
                </label>
              ) : (
                <label key={field.name}>
                  {field.label}
                  <input
                    type={field.type || 'text'}
                    required={field.required}
                    step={field.type === 'number' ? 'any' : undefined}
                    min={field.type === 'number' ? '0' : undefined}
                    disabled={editingId !== null && field.locked}
                    value={form[field.name]}
                    onChange={(event) => setForm({ ...form, [field.name]: event.target.value })}
                  />
                </label>
              )))}
              <div className="row-actions">
                <button className="primary-button" type="submit">{editingId ? 'Save changes' : `Add ${noun.toLowerCase()}`}</button>
                {editingId && <button className="text-button" type="button" onClick={reset}>Cancel</button>}
              </div>
            </form>
          </Panel>
        )}
        {error && <p className="error-message">{error}</p>}
        {message && <p className="success-message">{message}</p>}
        <Panel title={hero.title}>
          <LoadState state={records} empty={`No ${noun.toLowerCase()} records yet.`}>
            {records.data && <DataTable columns={[...columns, ...actions]} rows={records.data} />}
          </LoadState>
        </Panel>
      </div>
    </>
  )
}
