import axios from 'axios'

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 120000,
  headers: {
    'Content-Type': 'application/json',
  },
})

export function clearSession() {
  localStorage.removeItem('traceintel.user')
  localStorage.removeItem('traceintel.token')
  localStorage.removeItem('traceintel.expires')
}

function endSession() {
  clearSession()
  window.location.assign('/login')
}

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('traceintel.token')
  if (token) {
    if (Date.now() > Number(localStorage.getItem('traceintel.expires'))) {
      endSession()
      return Promise.reject(new Error('Your session has expired.'))
    }
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(undefined, (error) => {
  const isLogin = error.config?.url === '/auth/login'
  if (error.response?.status === 401 && !isLogin) {
    endSession()
  }
  return Promise.reject(error)
})

export function errorMessage(error) {
  const { response } = error
  if (error.code === 'ECONNABORTED') return 'The server took too long to answer. Wait a minute and try again.'
  if (!response) return 'The server could not be reached. Check that the backend is running.'
  if (Object.keys(response.data?.validationErrors || {}).length) return Object.values(response.data.validationErrors).join(' ')
  if (response.data?.message) return response.data.message
  if (response.status === 403) return 'Your role does not have access to this.'
  return 'Something went wrong. Please try again.'
}
