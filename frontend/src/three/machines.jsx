import { useMemo, useRef } from 'react'
import { useFrame } from '@react-three/fiber'
import { Vector3 } from 'three'

const RED = '#e31837'
const CHARCOAL = '#242424'
const STEEL = '#8a8d91'
const LIGHT = '#d9d9d9'

function Box({ size, color = CHARCOAL, ...props }) {
  return (
    <mesh castShadow receiveShadow {...props}>
      <boxGeometry args={size} />
      <meshStandardMaterial color={color} metalness={0.25} roughness={0.5} />
    </mesh>
  )
}

function Cylinder({ radius, height, color = STEEL, ...props }) {
  return (
    <mesh castShadow {...props}>
      <cylinderGeometry args={[radius, radius, height, 32]} />
      <meshStandardMaterial color={color} metalness={0.3} roughness={0.45} />
    </mesh>
  )
}

function TagAnchor({ tagRef, position }) {
  const anchor = useRef()
  const point = useMemo(() => new Vector3(), [])

  useFrame(({ camera, size }) => {
    if (!tagRef.current) return
    anchor.current.getWorldPosition(point).project(camera)
    const x = ((point.x + 1) / 2) * size.width
    const y = ((1 - point.y) / 2) * size.height
    tagRef.current.style.transform = `translate(-50%, -50%) translate(${x}px, ${y}px)`
  })

  return <group ref={anchor} position={position} />
}

function ArmBody({ parts }) {
  return (
    <>
      <Cylinder radius={0.95} height={0.3} color={CHARCOAL} position={[0, 0.15, 0]} />
      <group ref={parts.turret} position={[0, 0.3, 0]}>
        <Cylinder radius={0.6} height={0.7} color={RED} position={[0, 0.35, 0]} />
        <group ref={parts.shoulder} position={[0, 0.8, 0]}>
          <Cylinder radius={0.32} height={0.8} rotation={[Math.PI / 2, 0, 0]} />
          <Box size={[0.42, 2, 0.42]} color={RED} position={[0, 1, 0]} />
          <group ref={parts.elbow} position={[0, 2, 0]}>
            <Cylinder radius={0.26} height={0.6} rotation={[Math.PI / 2, 0, 0]} />
            <Box size={[0.3, 1.7, 0.3]} color={LIGHT} position={[0, 0.85, 0]} />
            <group ref={parts.wrist} position={[0, 1.7, 0]}>
              <Cylinder radius={0.17} height={0.3} color={CHARCOAL} />
              {parts.tool}
            </group>
          </group>
        </group>
      </group>
    </>
  )
}

function useArm(scroll, speed, tool) {
  const turret = useRef()
  const shoulder = useRef()
  const elbow = useRef()
  const wrist = useRef()

  useFrame(({ clock }) => {
    const time = clock.elapsedTime * speed
    const travel = scroll?.current ?? 0
    turret.current.rotation.y = Math.sin(time * 0.6) * 0.8 + travel * Math.PI * 2
    shoulder.current.rotation.z = -0.3 + Math.sin(time * 0.9) * 0.28 + travel * 0.3
    elbow.current.rotation.z = -1.05 + Math.sin(time * 1.2) * 0.35
    wrist.current.rotation.z = -0.6 + Math.sin(time * 1.5) * 0.3
  })

  return { turret, shoulder, elbow, wrist, tool }
}

export function RoboticArm({ scroll, tagRef }) {
  const leftFinger = useRef()
  const rightFinger = useRef()
  const parts = useArm(scroll, 1, (
    <>
      <Box size={[0.5, 0.1, 0.2]} position={[0, 0.2, 0]} />
      <Box ref={leftFinger} size={[0.07, 0.34, 0.14]} color={RED} position={[-0.2, 0.4, 0]} />
      <Box ref={rightFinger} size={[0.07, 0.34, 0.14]} color={RED} position={[0.2, 0.4, 0]} />
    </>
  ))

  useFrame(({ clock }) => {
    const gap = 0.14 + Math.abs(Math.sin(clock.elapsedTime * 1.2)) * 0.09
    leftFinger.current.position.x = -gap
    rightFinger.current.position.x = gap
  })

  return (
    <group>
      <ArmBody parts={parts} />
      {tagRef && <TagAnchor tagRef={tagRef} position={[-1.6, 3.7, 0]} />}
    </group>
  )
}

export function WeldingRobot({ scroll, tagRef }) {
  const spark = useRef()
  const flash = useRef()
  const parts = useArm(scroll, 0.7, (
    <>
      <Cylinder radius={0.09} height={0.55} color={LIGHT} position={[0, 0.3, 0]} />
      <mesh ref={spark} position={[0, 0.65, 0]}>
        <sphereGeometry args={[0.1, 12, 12]} />
        <meshBasicMaterial color="#ffd9a0" />
      </mesh>
      <pointLight ref={flash} position={[0, 0.7, 0]} color="#ffb066" distance={5} />
    </>
  ))

  useFrame(({ clock }) => {
    const arc = Math.max(0, Math.sin(clock.elapsedTime * 31) * Math.sin(clock.elapsedTime * 2.3))
    spark.current.scale.setScalar(0.4 + arc * 1.3)
    flash.current.intensity = arc * 24
  })

  return (
    <group>
      <ArmBody parts={parts} />
      <Box size={[1.6, 1.1, 0.12]} color={STEEL} position={[1.9, 1.5, 0]} rotation={[0, 0.3, 0.1]} />
      <Box size={[0.3, 1, 0.3]} position={[1.9, 0.5, 0]} />
      {tagRef && <TagAnchor tagRef={tagRef} position={[-1.6, 3.7, 0]} />}
    </group>
  )
}

export function CncMachine({ scroll, tagRef }) {
  const carriage = useRef()
  const spindle = useRef()
  const table = useRef()

  useFrame(({ clock }) => {
    const time = clock.elapsedTime
    const travel = scroll?.current ?? 0
    carriage.current.position.x = Math.sin(time * 0.8 + travel * 6) * 1.2
    spindle.current.position.y = 1.9 + Math.abs(Math.sin(time * 1.1)) * 0.35
    spindle.current.rotation.y = time * 22
    table.current.position.z = Math.sin(time * 0.5) * 0.35
  })

  return (
    <group>
      <Box size={[4.4, 0.6, 2.6]} position={[0, 0.3, 0]} />
      <Box size={[0.3, 3, 0.4]} color={RED} position={[-2.05, 2, -0.9]} />
      <Box size={[0.3, 3, 0.4]} color={RED} position={[2.05, 2, -0.9]} />
      <Box size={[4.4, 0.4, 0.4]} color={LIGHT} position={[0, 3.3, -0.9]} />
      <group ref={table} position={[0, 0.7, 0]}>
        <Box size={[3, 0.18, 1.7]} color={STEEL} />
        <Box size={[1, 0.45, 0.8]} color={LIGHT} position={[0, 0.3, 0]} />
      </group>
      <group ref={carriage} position={[0, 0, -0.1]}>
        <Box size={[0.8, 0.9, 0.9]} position={[0, 2.9, -0.35]} />
        <group ref={spindle} position={[0, 1.9, 0]}>
          <Cylinder radius={0.16} height={1} position={[0, 0.4, 0]} />
          <Cylinder radius={0.05} height={0.5} color={RED} position={[0, -0.3, 0]} />
        </group>
      </group>
      {tagRef && <TagAnchor tagRef={tagRef} position={[-2.4, 3.9, 0]} />}
    </group>
  )
}

function ConveyorItem({ offset, length, scroll, children }) {
  const item = useRef()
  useFrame(({ clock }) => {
    const travel = (scroll?.current ?? 0) * length
    const place = (clock.elapsedTime * 0.75 + offset + travel) % length
    item.current.position.x = place - length / 2
  })
  return <group ref={item} position={[0, 0.95, 0]}>{children}</group>
}

export function ConveyorSystem({ scroll, tagRef }) {
  const length = 8
  const rollerRefs = useRef([])
  const rollers = useMemo(() => Array.from({ length: 17 }, (_, index) => -3.9 + index * 0.49), [])

  useFrame((_, delta) => {
    rollerRefs.current.forEach((roller) => { roller.rotation.y += delta * 5 })
  })

  return (
    <group>
      <Box size={[8.2, 0.14, 0.14]} position={[0, 0.7, 0.72]} color={RED} />
      <Box size={[8.2, 0.14, 0.14]} position={[0, 0.7, -0.72]} color={RED} />
      {[-3.6, 0, 3.6].map((x) => (
        <group key={x}>
          <Box size={[0.16, 0.7, 0.16]} position={[x, 0.33, 0.72]} />
          <Box size={[0.16, 0.7, 0.16]} position={[x, 0.33, -0.72]} />
        </group>
      ))}
      {rollers.map((x, index) => (
        <group key={x} position={[x, 0.76, 0]} rotation={[Math.PI / 2, 0, 0]}>
          <group ref={(roller) => { rollerRefs.current[index] = roller }}>
            <Cylinder radius={0.09} height={1.4} />
          </group>
        </group>
      ))}
      <ConveyorItem offset={0} length={length} scroll={scroll}><Box size={[0.9, 0.3, 0.7]} color={LIGHT} /></ConveyorItem>
      <ConveyorItem offset={2.7} length={length} scroll={scroll}><Cylinder radius={0.33} height={0.34} color={STEEL} /></ConveyorItem>
      <ConveyorItem offset={5.3} length={length} scroll={scroll}><Box size={[0.7, 0.42, 0.6]} color={RED} /></ConveyorItem>
      {tagRef && <TagAnchor tagRef={tagRef} position={[-3, 2.1, 0]} />}
    </group>
  )
}

function Gear({ radius, teeth, speed, ...props }) {
  const gear = useRef()
  const toothAngles = useMemo(() => Array.from({ length: teeth }, (_, index) => (index / teeth) * Math.PI * 2), [teeth])

  useFrame((_, delta) => { gear.current.rotation.z += delta * speed })

  return (
    <group ref={gear} {...props}>
      <Cylinder radius={radius} height={0.2} rotation={[Math.PI / 2, 0, 0]} />
      {toothAngles.map((angle) => (
        <Box
          key={angle}
          size={[0.2, 0.17, 0.2]}
          color={STEEL}
          position={[Math.cos(angle) * (radius + 0.07), Math.sin(angle) * (radius + 0.07), 0]}
          rotation={[0, 0, angle]}
        />
      ))}
      <Cylinder radius={radius * 0.3} height={0.26} color={RED} rotation={[Math.PI / 2, 0, 0]} />
    </group>
  )
}

export function AssemblyUnit({ scroll, tagRef }) {
  const ram = useRef()
  const part = useRef()

  useFrame(({ clock }) => {
    const stroke = Math.pow(Math.max(0, Math.sin(clock.elapsedTime * 1.4)), 3)
    ram.current.position.y = 2.75 - stroke * 0.95
    part.current.scale.y = 1 - stroke * 0.2
    part.current.rotation.y = (scroll?.current ?? 0) * Math.PI * 2
  })

  return (
    <group>
      <Box size={[3.4, 0.5, 2.2]} position={[0, 0.25, 0]} />
      <Box size={[0.36, 3.5, 0.36]} color={RED} position={[-1.4, 2, 0]} />
      <Box size={[0.36, 3.5, 0.36]} color={RED} position={[1.4, 2, 0]} />
      <Box size={[3.2, 0.5, 1]} color={LIGHT} position={[0, 3.75, 0]} />
      <group ref={ram} position={[0, 2.75, 0]}>
        <Cylinder radius={0.2} height={1.5} position={[0, 0.6, 0]} />
        <Box size={[1.3, 0.3, 1]} color={STEEL} position={[0, -0.2, 0]} />
      </group>
      <group ref={part} position={[0, 0.72, 0]}>
        <Box size={[1, 0.44, 0.8]} color={STEEL} />
      </group>
      <Gear radius={0.65} teeth={12} speed={1} position={[2.75, 1.4, 0]} />
      <Gear radius={0.42} teeth={8} speed={-1.55} position={[2.75, 2.55, 0]} />
      {tagRef && <TagAnchor tagRef={tagRef} position={[-2.3, 4, 0]} />}
    </group>
  )
}

export const MACHINE_MODELS = {
  arm: RoboticArm,
  cnc: CncMachine,
  conveyor: ConveyorSystem,
  welder: WeldingRobot,
  assembly: AssemblyUnit,
}
