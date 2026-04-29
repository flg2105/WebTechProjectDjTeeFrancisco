<template>
  <section class="page-stack auth-page">
    <div class="auth-shell">
      <div class="auth-intro">
        <p class="eyebrow">Secure Access</p>
        <h1>Project Pulse</h1>
        <p class="hero-text">
          Sign in to manage sections, review reports, and keep weekly student work moving.
        </p>
      </div>

      <form class="surface-card auth-card" @submit.prevent="submitLogin">
        <div class="auth-header">
          <p class="eyebrow">Login</p>
          <h2>Welcome back</h2>
          <p class="helper">Use your Project Pulse account to continue.</p>
        </div>

        <p v-if="errorMessage" class="notice error">{{ errorMessage }}</p>

        <label>
          Email
          <input v-model.trim="form.email" autocomplete="username" required type="email" />
        </label>

        <label>
          Password
          <input v-model="form.password" autocomplete="current-password" required type="password" />
        </label>

        <button class="primary-button auth-submit" :disabled="isSubmitting" type="submit">
          {{ isSubmitting ? 'Signing in...' : 'Sign In' }}
        </button>
      </form>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authSession } from '../../shared/services/authSession'

const router = useRouter()
const route = useRoute()

const isSubmitting = ref(false)
const errorMessage = ref('')
const form = reactive({
  email: '',
  password: ''
})

async function submitLogin() {
  isSubmitting.value = true
  errorMessage.value = ''

  try {
    await authSession.login(form.email, form.password)
    const redirectTo = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    await router.replace(redirectTo)
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.auth-page {
  align-items: center;
  justify-items: center;
  min-height: calc(100vh - 2rem);
  min-width: 0;
}

.auth-shell {
  align-items: center;
  display: grid;
  gap: 1.35rem;
  justify-items: center;
  width: min(100%, 28rem);
}

.auth-intro {
  display: grid;
  gap: 0.55rem;
  min-width: 0;
  text-align: center;
  width: 100%;
}

.auth-card {
  align-self: start;
  display: grid;
  gap: 1.05rem;
  min-width: 0;
  padding: 1.5rem;
  width: 100%;
}

.auth-header {
  display: grid;
  gap: 0.35rem;
}

.auth-submit {
  justify-content: center;
  width: 100%;
}

@media (max-width: 760px) {
  .auth-page {
    min-height: auto;
  }

  .auth-card {
    padding: 1.2rem;
  }
}
</style>
