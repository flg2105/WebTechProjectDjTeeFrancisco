<template>
  <section class="page-stack auth-page">
    <div class="hero-banner auth-hero">
      <div class="hero-copy">
        <p class="eyebrow">Secure Access</p>
        <h1>Sign in to Project Pulse.</h1>
        <p class="hero-text">
          Use your Project Pulse account to continue into the admin, instructor, or student
          workflow assigned to you.
        </p>
      </div>

      <form class="surface-card auth-card" @submit.prevent="submitLogin">
        <div class="section-heading">
          <div>
            <p class="eyebrow">Login</p>
            <h2>HTTP Basic to JWT</h2>
          </div>
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

        <button class="primary-button" :disabled="isSubmitting" type="submit">
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
