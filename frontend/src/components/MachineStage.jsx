import { lazy, Suspense, useEffect, useRef, useState } from 'react'
import { MachineTag } from './MachineTag'

const MachineScene = lazy(() => import('../three/MachineScene'))

export function supportsWebGL() {
  const canvas = document.createElement('canvas')
  return Boolean(canvas.getContext('webgl2') || canvas.getContext('webgl'))
}

export function prefersReducedMotion() {
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

export function MachineStage({ machine, scroll }) {
  const stage = useRef(null)
  const tagRef = useRef(null)
  const [onScreen, setOnScreen] = useState(true)
  const [canRender] = useState(supportsWebGL)
  const [reducedMotion] = useState(prefersReducedMotion)

  useEffect(() => {
    const observer = new IntersectionObserver(([entry]) => setOnScreen(entry.isIntersecting))
    observer.observe(stage.current)
    return () => observer.disconnect()
  }, [])

  return (
    <div className="machine-stage" ref={stage}>
      {canRender && (
        <Suspense fallback={null}>
          <MachineScene machine={machine} scroll={scroll} tagRef={tagRef} active={onScreen} reducedMotion={reducedMotion} />
        </Suspense>
      )}
      <MachineTag machine={machine} tagRef={canRender ? tagRef : undefined} />
    </div>
  )
}
