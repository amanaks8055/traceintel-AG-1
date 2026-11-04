import { Canvas } from '@react-three/fiber'
import { OrbitControls } from '@react-three/drei'
import { MACHINE_MODELS } from './machines'

function Lights() {
  return (
    <>
      <ambientLight intensity={1.5} />
      <directionalLight position={[6, 9, 6]} intensity={2.4} />
      <pointLight position={[-5, 3, 3]} intensity={30} color="#e31837" />
    </>
  )
}

export default function MachineScene({ machine, scroll, tagRef, active, reducedMotion }) {
  const Model = MACHINE_MODELS[machine]
  const canDrag = window.innerWidth > 800

  return (
    <Canvas
      camera={{ position: [5.5, 3.8, 8], fov: 38 }}
      dpr={[1, 1.5]}
      frameloop={reducedMotion ? 'demand' : active ? 'always' : 'never'}
    >
      <Lights />
      <group position={[0, -1.5, 0]}>
        <Model scroll={scroll} tagRef={tagRef} />
        <gridHelper args={[20, 20, '#4a4a4a', '#303030']} />
      </group>
      {canDrag && <OrbitControls enablePan={false} enableZoom={false} minPolarAngle={0.9} maxPolarAngle={1.5} />}
    </Canvas>
  )
}
