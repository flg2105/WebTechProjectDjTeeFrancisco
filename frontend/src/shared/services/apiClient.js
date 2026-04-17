const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

async function request(path, options) {
  const res = await fetch(`${baseUrl}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options?.headers || {})
    },
    ...options
  })

  const body = await res.json().catch(() => null)
  if (!res.ok) {
    const message = body?.message || `Request failed (${res.status})`
    throw new Error(message)
  }

  return body
}

export const apiClient = {
  get: (path) => request(path),
  post: (path, data) => request(path, { method: 'POST', body: JSON.stringify(data) }),
  put: (path, data) => request(path, { method: 'PUT', body: JSON.stringify(data) }),
  del: (path) => request(path, { method: 'DELETE' })
}

