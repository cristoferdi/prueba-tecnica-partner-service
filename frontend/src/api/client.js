const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'

export class ApiError extends Error {
  constructor(message, status, payload) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.payload = payload
  }
}

export const apiFetch = async (path, options = {}) => {
  const token = localStorage.getItem('token')

  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(options.headers || {}),
  }

  const config = {
    ...options,
    headers,
  }

  const response = await fetch(`${BASE_URL}${path}`, config)

  const contentType = response.headers.get('content-type') || ''
  let payload
  if (contentType.includes('application/json')) {
    payload = await response.json()
  }

  if (!response.ok) {
    const message =
      (payload && (payload.message || payload.error)) ||
      `Error ${response.status}`
    throw new ApiError(message, response.status, payload)
  }

  return payload
}

export const login = async (username, password) => {
  const payload = await apiFetch('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
  return { token: payload.token, role: payload.role }
}
