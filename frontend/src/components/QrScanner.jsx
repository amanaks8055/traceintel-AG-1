import { useEffect, useRef, useState } from 'react'

export function QrScanner({ onScan }) {
  const handler = useRef(onScan)
  const [error, setError] = useState('')

  useEffect(() => {
    handler.current = onScan
  }, [onScan])

  useEffect(() => {
    let scanner = null
    let stopped = false

    import('html5-qrcode').then(({ Html5Qrcode }) => {
      if (stopped) return null
      scanner = new Html5Qrcode('qr-reader')
      return scanner.start({ facingMode: 'environment' }, { fps: 10, qrbox: 220 }, (text) => handler.current(text))
    }).catch(() => setError('The camera could not be started. Check the browser permission, or type the serial.'))

    return () => {
      stopped = true
      if (scanner?.isScanning) scanner.stop().catch(() => {})
    }
  }, [])

  return (
    <div className="wide">
      <div id="qr-reader" className="qr-reader" />
      {error && <p className="error-message">{error}</p>}
    </div>
  )
}
