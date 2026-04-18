const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

class ApiError extends Error {
  constructor(message, result, status) {
    super(message)
    this.name = 'ApiError'
    this.result = result
    this.status = status
  }
}

async function request(path, options) {
  const res = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(options?.headers || {})
    }
  })

  const body = await res.json().catch(() => null)
  if (!res.ok || body?.flag === false) {
    const message = body?.message || `Request failed (${res.status})`
    throw new ApiError(message, body, res.status)
  }

  return body
}

export const apiClient = {
  get: (path) => request(path),
  post: (path, data) => request(path, { method: 'POST', body: JSON.stringify(data) }),
  put: (path, data) => request(path, { method: 'PUT', body: JSON.stringify(data) }),
  del: (path) => request(path, { method: 'DELETE' })
}

export { ApiError }
