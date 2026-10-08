const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '').replace(/\/+$/, '')

export async function apiRequest(path, options = {}) {
  if (typeof path !== 'string' || !path.startsWith('/')) throw new Error('API path must be a relative path starting with /')

  const headers = new Headers(options.headers ?? {})
  if (options.body) headers.set('Content-Type', 'application/json')
  if (options.token) headers.set('Authorization', `Bearer ${options.token}`)

  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers })
  if (response.status === 204) return null

  const payload = await response.json().catch(() => null)
  if (!response.ok) {
    const details = payload?.errors
    const validationMessage = Array.isArray(details)
      ? details.map((item) => item.defaultMessage).filter(Boolean).join(', ')
      : details && typeof details === 'object'
        ? Object.entries(details).map(([field, message]) => `${field}: ${message}`).join(', ')
        : null
    const message = (payload?.message && payload.message !== 'Validation failed' ? payload.message : null)
      ?? validationMessage
      ?? payload?.error
      ?? `Request failed (${response.status})`
    const error = new Error(message)
    error.status = response.status
    throw error
  }
  return payload
}

export function readSavedSession() {
  try {
    const session = JSON.parse(localStorage.getItem('folio-session') ?? 'null')
    return session?.token ? session : null
  } catch {
    return null
  }
}

export function saveSession(session) {
  localStorage.setItem('folio-session', JSON.stringify(session))
}

export function clearSession() {
  localStorage.removeItem('folio-session')
}

export function formatDate(value) {
  if (!value) return 'Date unavailable'
  return new Date(value).toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  })
}

export function humanizeStatus(value = 'APPLIED') {
  return value.toLowerCase().replaceAll('_', ' ').replace(/\b\w/g, (letter) => letter.toUpperCase())
}

export function getInitials(value = '') {
  return value
    .split(/[\s@._-]+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0].toUpperCase())
    .join('') || 'F'
}

export function getSkills(value = '') {
  return value.split(',').map((skill) => skill.trim()).filter(Boolean)
}
