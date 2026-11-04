import { useRef } from 'react'
import { Canvas, useFrame } from '@react-three/fiber'
import { AssemblyUnit, ConveyorSystem, CncMachine, RoboticArm, WeldingRobot } from './machines'

const SPACING = 14
const STATIONS = [RoboticArm, CncMachine, WeldingRobot, ConveyorSystem, AssemblyUnit]

function Camera({ scroll, onStation }) {
  const lastStation = useRef(0)

  useFrame(({ camera }) => {
    const place = scroll.current * (STATIONS.length - 1)
    const x = place * SPACING
    camera.position.set(x + 5 - Math.sin(place * Math.PI) * 2, 3.2, 9)
    camera.lookAt(x - 2.8, 0.6, 0)

    const station = Math.round(place)
    if (station !== lastStation.current) {
      lastStation.current = station
      onStation(station)
    }
  })

  return null
}

export default function PlantTourScene({ scroll, station, tagRef, onStation, reducedMotion }) {
  return (
    <Canvas camera={{ position: [5, 3.2, 9], fov: 38 }} dpr={[1, 1.5]} frameloop={reducedMotion ? 'demand' : 'always'}>
      <color attach="background" args={['#171717']} />
      <fog attach="fog" args={['#171717', 11, 30]} />
      <ambientLight intensity={1.5} />
      <directionalLight position={[20, 10, 8]} intensity={2.4} />
      <pointLight position={[0, 5, 5]} intensity={30} color="#e31837" />
      <Camera scroll={scroll} onStation={onStation} />
      <group position={[0, -1.5, 0]}>
        {STATIONS.map((Machine, index) => (
          <group key={index} position={[index * SPACING, 0, 0]}>
            <Machine scroll={scroll} tagRef={index === station ? tagRef : undefined} />
          </group>
        ))}
        <gridHelper args={[90, 90, '#4a4a4a', '#2c2c2c']} position={[SPACING * 2, 0, 0]} />
      </group>
    </Canvas>
  )
}
