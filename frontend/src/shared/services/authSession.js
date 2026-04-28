import { reactive } from 'vue'

const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const TOKEN_KEY = 'project-pulse.jwt'
const USER_KEY = 'project-pulse.user'

const state = reactive({
  token: '',
  currentUser: null
})

function safeParse(value) {
  try {
    return JSON.parse(value)
  } catch {
    return null
  }
}

function persist() {
  if (state.token) {
    localStorage.setItem(TOKEN_KEY, state.token)
  } else {
    localStorage.removeItem(TOKEN_KEY)
  }

  if (state.currentUser) {
    localStorage.setItem(USER_KEY, JSON.stringify(state.currentUser))
  } else {
    localStorage.removeItem(USER_KEY)
  }
}

function applyAuth(data) {
  state.token = data.accessToken
  state.currentUser = {
    userId: data.userId,
    email: data.email,
    displayName: data.displayName,
    role: data.role,
    status: data.status
  }
  persist()
}

async function parseResult(res) {
  const body = await res.json().catch(() => null)
  if (!res.ok || body?.flag === false) {
    const message = body?.message || `Request failed (${res.status})`
    const error = new Error(message)
    error.status = res.status
    error.result = body
    throw error
  }
  return body
}

function authHeaders() {
  return state.token ? { Authorization: `Bearer ${state.token}` } : {}
}

export const authSession = {
  state,

  hydrate() {
    state.token = localStorage.getItem(TOKEN_KEY) || ''
    state.currentUser = safeParse(localStorage.getItem(USER_KEY))
  },

  get token() {
    return state.token
  },

  get currentUser() {
    return state.currentUser
  },

  get isAuthenticated() {
    return Boolean(state.token && state.currentUser)
  },

  async login(email, password) {
    const encoded = btoa(`${email}:${password}`)
    const res = await fetch(`${baseUrl}/api/auth/login`, {
      method: 'POST',
      headers: {
        Authorization: `Basic ${encoded}`
      }
    })

    const body = await parseResult(res)
    applyAuth(body.data)
    return body.data
  },

  async refreshCurrentUser() {
    if (!state.token) {
      state.currentUser = null
      persist()
      return null
    }

    const res = await fetch(`${baseUrl}/api/auth/me`, {
      headers: authHeaders()
    })

    const body = await parseResult(res)
    state.currentUser = body.data
    persist()
    return state.currentUser
  },

  logout() {
    state.token = ''
    state.currentUser = null
    persist()
  }
}
