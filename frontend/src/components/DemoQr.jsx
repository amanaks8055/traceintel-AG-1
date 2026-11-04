import { useEffect, useState } from 'react'
import { apiClient, errorMessage } from '../api/client'
import { useLoad } from '../api/useLoad'

export function DemoQr({ batchId, onScan }) {
  const serials = useLoad(`/batches/${batchId}/serials`)
  const [chosen, setChosen] = useState('')
  const [image, setImage] = useState(null)
  const [error, setError] = useState('')

  const serial = chosen || serials.data?.[0]?.serial || ''
  const shown = image?.serial === serial ? image : null

  useEffect(() => {
    if (!serial) return undefined
    let stopped = false
    let url = null

    apiClient.get(`/qr/${encodeURIComponent(serial)}`, { responseType: 'blob' })
      .then((response) => {
        if (stopped) return
        url = URL.createObjectURL(response.data)
        setImage({ serial, url, blob: response.data })
      })
      .catch((failure) => setError(errorMessage(failure)))

    return () => {
      stopped = true
      if (url) URL.revokeObjectURL(url)
    }
  }, [serial])

  async function scan() {
    setError('')
    try {
      const { Html5Qrcode } = await import('html5-qrcode')
      const file = new File([shown.blob], 'demo-qr.png', { type: 'image/png' })
      onScan(await new Html5Qrcode('demo-qr-reader').scanFile(file, false))
    } catch {
      setError('This QR could not be read. Type the serial instead.')
    }
  }

  if (serials.loading) return null
  if (serials.error) return <p className="error-message wide">{serials.error}</p>
  if (!serials.data.length) return <p className="wide">This batch has no serials yet. A supervisor can generate them on Production batches.</p>

  return (
    <div className="wide demo-qr">
      <div id="demo-qr-reader" hidden />
      {shown && (
        <figure className="qr-preview">
          <img src={shown.url} alt={`Demo QR for ${serial}`} />
          <figcaption>{serial}</figcaption>
        </figure>
      )}
      <div className="demo-qr-actions">
        <label>
          Demo QR
          <select value={serial} onChange={(event) => setChosen(event.target.value)}>
            {serials.data.map((item) => <option key={item.id} value={item.serial}>{item.serial}</option>)}
          </select>
        </label>
        <button className="text-button" disabled={!shown} type="button" onClick={scan}>Scan this QR</button>
        {error && <p className="error-message">{error}</p>}
      </div>
    </div>
  )
}
