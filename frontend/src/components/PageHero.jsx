import { useEffect, useRef } from 'react'
import { motion } from 'framer-motion'
import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import { MachineStage } from './MachineStage'

gsap.registerPlugin(ScrollTrigger)

export function PageHero({ eyebrow, title, description, machine }) {
  const scroll = useRef(0)

  useEffect(() => {
    const trigger = ScrollTrigger.create({
      start: 0,
      end: 'max',
      onUpdate: (self) => { scroll.current = self.progress },
    })
    return () => trigger.kill()
  }, [])

  return (
    <header className="page-hero">
      <motion.div className="page-hero-text" initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.45 }}>
        <p className="eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
        <p>{description}</p>
      </motion.div>
      <MachineStage machine={machine} scroll={scroll} />
    </header>
  )
}
