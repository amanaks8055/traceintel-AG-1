import { useCallback, useEffect, useState } from 'react'
import { apiClient, errorMessage } from './client'

export function useLoad(path) {
  const [state, setState] = useState({ data: null, error: '', loading: true })

  const reload = useCallback(() => {
    return apiClient.get(path)
      .then((response) => setState({ data: response.data, error: '', loading: false }))
      .catch((error) => setState({ data: null, error: errorMessage(error), loading: false }))
  }, [path])

  useEffect(() => {
    reload()
  }, [reload])

  return { ...state, reload }
}
