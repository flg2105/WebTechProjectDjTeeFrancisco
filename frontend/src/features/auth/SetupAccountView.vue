<template>
  <section class="page-stack auth-page">
    <div class="auth-shell setup-shell">
      <div class="auth-intro">
        <p class="eyebrow">Account Setup</p>
        <h1>{{ pageTitle }}</h1>
        <p class="hero-text">{{ pageDescription }}</p>
      </div>

      <form class="surface-card auth-card" @submit.prevent="submitSetup">
        <div class="auth-header">
          <p class="eyebrow">{{ roleLabel }}</p>
          <h2>Create your account</h2>
          <p class="helper">Use the invited email address and choose a password with at least 8 characters.</p>
        </div>

        <p v-if="successMessage" class="notice success">{{ successMessage }}</p>
        <p v-if="errorMessage" class="notice error">{{ errorMessage }}</p>

        <label>
          Display name
          <input v-model.trim="form.displayName" required />
        </label>

        <label>
          Email
          <input v-model.trim="form.email" required type="email" />
        </label>

        <label>
          Password
          <input v-model="form.password" minlength="8" required type="password" />
        </label>

        <label>
          Confirm password
          <input v-model="form.confirmPassword" minlength="8" required type="password" />
        </label>

        <button class="primary-button auth-submit" :disabled="isSubmitting" type="submit">
          {{ isSubmitting ? 'Creating...' : submitLabel }}
        </button>

        <RouterLink class="ghost-button auth-submit" to="/login">Back to Login</RouterLink>
      </form>
    </div>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { usersService } from '../users/usersService'

const props = defineProps({
  role: {
    type: String,
    required: true
  }
})

const router = useRouter()
const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const form = reactive({
  displayName: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const isStudent = computed(() => props.role === 'student')
const roleLabel = computed(() => (isStudent.value ? 'Student Setup' : 'Instructor Setup'))
const pageTitle = computed(() => (isStudent.value ? 'Finish your student account setup.' : 'Finish your instructor account setup.'))
const pageDescription = computed(() =>
  isStudent.value
    ? 'Create the account you will use for weekly activity reports and peer evaluations.'
    : 'Create the account you will use to supervise teams and review reports.'
)
const submitLabel = computed(() => (isStudent.value ? 'Create Student Account' : 'Create Instructor Account'))

function resetForm() {
  form.displayName = ''
  form.email = ''
  form.password = ''
  form.confirmPassword = ''
}

async function submitSetup() {
  if (form.password !== form.confirmPassword) {
    errorMessage.value = 'Passwords do not match.'
    return
  }

  isSubmitting.value = true
  errorMessage.value = ''
  successMessage.value = ''

  try {
    const payload = {
      displayName: form.displayName,
      email: form.email,
      password: form.password
    }
    if (isStudent.value) {
      await usersService.setupStudent(payload)
    } else {
      await usersService.setupInstructor(payload)
    }

    successMessage.value = `${isStudent.value ? 'Student' : 'Instructor'} account created. Redirecting to login...`
    resetForm()
    window.setTimeout(() => {
      router.replace('/login')
    }, 1200)
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

.setup-shell {
  width: min(100%, 32rem);
}
</style>
