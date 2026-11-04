import { lazy, Suspense, useEffect, useRef, useState } from 'react'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { prefersReducedMotion, supportsWebGL } from '../components/MachineStage'
import { MachineTag } from '../components/MachineTag'

const PlantTourScene = lazy(() => import('../three/PlantTourScene'))

gsap.registerPlugin(ScrollTrigger)

const stationMachines = ['arm', 'cnc', 'welder', 'conveyor', 'assembly']

const stops = [
  { eyebrow: 'Receiving', title: 'Every part starts with a known source.', text: 'Material lots are recorded on arrival, so each component can be followed back to its supplier.' },
  { eyebrow: 'Machining', title: 'Each batch keeps its history.', text: 'Batches record the part, machine, shift and the material lots they consumed.' },
  { eyebrow: 'Validation', title: 'The wrong part stops at the station.', text: 'Every scan is checked before the part moves on. Blocked scans are saved with a reason.' },
  { eyebrow: 'Dispatch', title: 'Shipments stay linked to production.', text: 'Batches connect to shipments, customers and the vehicles they end up in.' },
  { eyebrow: 'Traceability', title: 'One bad lot. Every affected vehicle.', text: 'Trace a lot forward to its VINs, or a VIN back to its supplier, with the time measured.' },
]

export function HomePage() {
  const { user } = useAuth()
  const page = useRef(null)
  const scroll = useRef(0)
  const tagRef = useRef(null)
  const [station, setStation] = useState(0)
  const [canRender] = useState(supportsWebGL)
  const [reducedMotion] = useState(prefersReducedMotion)

  useEffect(() => {
    if (reducedMotion) return undefined
    const context = gsap.context(() => {
      gsap.to(scroll, {
        current: 1,
        ease: 'none',
        scrollTrigger: { trigger: page.current, start: 'top top', end: 'bottom bottom', scrub: 0.8 },
      })
      gsap.utils.toArray('.tour-card').forEach((card) => {
        gsap.from(card, { opacity: 0, y: 50, scrollTrigger: { trigger: card, start: 'top 80%', end: 'top 45%', scrub: true } })
      })
    }, page)
    return () => context.revert()
  }, [reducedMotion])

  return (
    <div className="home" ref={page}>
      <div className="tour-canvas">
        {canRender && (
          <Suspense fallback={null}>
            <PlantTourScene scroll={scroll} station={station} tagRef={tagRef} onStation={setStation} reducedMotion={reducedMotion} />
          </Suspense>
        )}
        {canRender && <MachineTag machine={stationMachines[station]} tagRef={tagRef} />}
      </div>
      <header className="home-nav">
        <Link className="brand" to="/">TraceIntel</Link>
        {user
          ? <Link className="primary-button compact-button" to="/dashboard">Open dashboard</Link>
          : (
            <div>
              <Link className="text-link" to="/login">Sign in</Link>
              <Link className="primary-button compact-button" to="/signup">Sign up</Link>
            </div>
          )}
      </header>
      <section className="tour-intro">
        <p className="eyebrow">Manufacturing traceability</p>
        <h1>Walk the line from raw material to finished vehicle.</h1>
        <p>Scroll through the plant.</p>
      </section>
      {stops.map((stop) => (
        <section className="tour-stop" key={stop.title}>
          <div className="tour-card">
            <p className="eyebrow">{stop.eyebrow}</p>
            <h2>{stop.title}</h2>
            <p>{stop.text}</p>
          </div>
        </section>
      ))}
    </div>
  )
}
