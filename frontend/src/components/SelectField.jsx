import { useLoad } from '../api/useLoad'

export function SelectField({ label, path, value, onChange, optionLabel = (option) => option.label, required = true, className, groups }) {
  const options = useLoad(path)
  const render = (option) => <option key={option.id} value={option.id}>{optionLabel(option)}</option>

  return (
    <label className={className}>
      {label}
      <select required={required} value={value} onChange={(event) => onChange(event.target.value)}>
        <option value="">{options.loading ? 'Loading...' : options.error ? 'Could not load' : 'Choose...'}</option>
        {groups
          ? groups.map(([name, test], index) => (
            <optgroup key={name} label={name}>
              {options.data?.filter((option) => test(option) && !groups.slice(0, index).some(([, earlier]) => earlier(option))).map(render)}
            </optgroup>
          ))
          : options.data?.map(render)}
      </select>
    </label>
  )
}
