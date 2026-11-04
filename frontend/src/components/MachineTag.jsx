import { MACHINES } from '../three/machineList'

export function MachineTag({ machine, tagRef }) {
  return (
    <div className={tagRef ? 'tag-anchor' : 'tag-static'} ref={tagRef}>
      <div className="machine-tag">
        <span>{MACHINES[machine].name}</span>
        <strong>{MACHINES[machine].id}</strong>
      </div>
    </div>
  )
}
