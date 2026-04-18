<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

const loading = ref(true)
const lookupError = ref('')
const submitError = ref('')
const submitting = ref(false)
const stage = ref('edit')
const formRef = ref(null)

const invitation = reactive({
  token: '',
  role: '',
  email: '',
  firstName: '',
  lastName: '',
  alreadyRegistered: false
})

const form = reactive({
  firstName: '',
  middleInitial: '',
  lastName: '',
  password: '',
  confirmPassword: ''
})

const requiredRule = label => value => !!value || `${label} is required.`
const passwordRules = [
  value => !!value || 'Password is required.',
  value => value.length >= 8 || 'Password must be at least 8 characters.'
]
const middleInitialRules = [
  value => !value || /^[A-Za-z]$/.test(value) || 'Middle initial must be a single letter.'
]
const confirmPasswordRules = [
  value => !!value || 'Please reenter the password.',
  value => value === form.password || 'Reentered password must match the password.'
]

const title = computed(() => {
  if (invitation.alreadyRegistered) return 'Account already set up'
  if (stage.value === 'confirm') return 'Confirm your account details'
  if (stage.value === 'success') return 'Instructor account created'
  return 'Set up your instructor account'
})

async function loadInvitation() {
  loading.value = true
  lookupError.value = ''

  try {
    const response = await fetch(`${apiBaseUrl}/api/registrations/${route.params.token}`)
    const data = await response.json()

    if (!response.ok) {
      throw new Error(data.message ?? 'Unable to load registration invitation.')
    }

    Object.assign(invitation, data)
    Object.assign(form, {
      firstName: data.firstName ?? '',
      middleInitial: '',
      lastName: data.lastName ?? '',
      password: '',
      confirmPassword: ''
    })
  } catch (error) {
    lookupError.value = error.message
  } finally {
    loading.value = false
  }
}

async function continueToConfirmation() {
  submitError.value = ''
  const result = await formRef.value?.validate()
  if (result?.valid) {
    stage.value = 'confirm'
  }
}

function returnToEdit() {
  stage.value = 'edit'
}

async function submitRegistration() {
  submitting.value = true
  submitError.value = ''

  try {
    const response = await fetch(`${apiBaseUrl}/api/registrations/instructor`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        token: invitation.token,
        firstName: form.firstName,
        middleInitial: form.middleInitial,
        lastName: form.lastName,
        password: form.password,
        confirmPassword: form.confirmPassword
      })
    })

    const data = await response.json()

    if (!response.ok) {
      if (Array.isArray(data.fieldErrors) && data.fieldErrors.length > 0) {
        submitError.value = data.fieldErrors.map(fieldError => fieldError.message).join(' ')
      } else {
        submitError.value = data.message ?? 'Registration failed.'
      }
      return
    }

    stage.value = 'success'
    setTimeout(() => router.push(data.redirectPath ?? '/login'), 1200)
  } catch (error) {
    submitError.value = error.message
  } finally {
    submitting.value = false
  }
}

onMounted(loadInvitation)
</script>

<template>
  <v-main class="registration-shell">
    <v-container class="py-8 py-md-14">
      <v-row align="center" justify="center">
        <v-col cols="12" lg="10">
          <v-card class="rounded-xl overflow-hidden" elevation="10">
            <v-row no-gutters>
              <v-col cols="12" md="5" class="hero-panel">
                <div class="hero-copy">
                  <div class="text-overline mb-2">Project Pulse</div>
                  <h1 class="text-h3 font-weight-bold mb-4">UC-30 Instructor Registration</h1>
                  <p class="text-body-1">
                    This flow follows the use case: open invitation link, enter instructor details, validate input,
                    confirm registration, create the account, and redirect to login.
                  </p>
                  <v-chip class="mt-6" color="secondary" variant="flat">Invitation-based setup</v-chip>
                </div>
              </v-col>

              <v-col cols="12" md="7">
                <div class="pa-6 pa-md-10">
                  <div class="text-overline text-secondary mb-2">Instructor Journey</div>
                  <h2 class="text-h4 font-weight-bold mb-2">{{ title }}</h2>

                  <v-progress-circular
                    v-if="loading"
                    indeterminate
                    color="primary"
                    class="my-8"
                  />

                  <template v-else>
                    <v-alert v-if="lookupError" type="error" variant="tonal" class="mb-6">
                      {{ lookupError }}
                    </v-alert>

                    <template v-else-if="invitation.alreadyRegistered">
                      <v-alert type="info" variant="tonal" class="mb-6">
                        This instructor has already set up the account and should log in.
                      </v-alert>
                      <v-btn color="primary" size="large" @click="router.push('/login')">
                        Go to Login
                      </v-btn>
                    </template>

                    <template v-else-if="stage === 'edit'">
                      <v-form ref="formRef" class="mt-6">
                        <v-text-field
                          v-model="form.firstName"
                          label="First name"
                          variant="outlined"
                          :rules="[requiredRule('First name')]"
                          class="mb-3"
                        />

                        <v-text-field
                          v-model="form.middleInitial"
                          label="Middle initial"
                          variant="outlined"
                          maxlength="1"
                          :rules="middleInitialRules"
                          class="mb-3"
                        />

                        <v-text-field
                          v-model="form.lastName"
                          label="Last name"
                          variant="outlined"
                          :rules="[requiredRule('Last name')]"
                          class="mb-3"
                        />

                        <v-text-field
                          :model-value="invitation.email"
                          label="Invited email"
                          variant="outlined"
                          readonly
                          class="mb-3"
                        />

                        <v-text-field
                          v-model="form.password"
                          label="Password"
                          variant="outlined"
                          type="password"
                          :rules="passwordRules"
                          class="mb-3"
                        />

                        <v-text-field
                          v-model="form.confirmPassword"
                          label="Reenter password"
                          variant="outlined"
                          type="password"
                          :rules="confirmPasswordRules"
                        />

                        <v-alert v-if="submitError" type="error" variant="tonal" class="mt-4">
                          {{ submitError }}
                        </v-alert>

                        <div class="d-flex flex-wrap ga-3 mt-6">
                          <v-btn color="primary" size="large" @click="continueToConfirmation">
                            Continue
                          </v-btn>
                          <v-btn variant="text" size="large" @click="router.push('/login')">
                            Cancel
                          </v-btn>
                        </div>
                      </v-form>
                    </template>

                    <template v-else-if="stage === 'confirm'">
                      <v-list lines="two" class="rounded-lg border">
                        <v-list-item title="First name" :subtitle="form.firstName" />
                        <v-divider />
                        <v-list-item title="Middle initial" :subtitle="form.middleInitial || 'None provided'" />
                        <v-divider />
                        <v-list-item title="Last name" :subtitle="form.lastName" />
                        <v-divider />
                        <v-list-item title="Email" :subtitle="invitation.email" />
                      </v-list>

                      <v-alert v-if="submitError" type="error" variant="tonal" class="mt-4">
                        {{ submitError }}
                      </v-alert>

                      <div class="d-flex flex-wrap ga-3 mt-6">
                        <v-btn color="primary" size="large" :loading="submitting" @click="submitRegistration">
                          Confirm Registration
                        </v-btn>
                        <v-btn variant="outlined" size="large" @click="returnToEdit">
                          Modify Details
                        </v-btn>
                      </div>
                    </template>

                    <template v-else-if="stage === 'success'">
                      <v-alert type="success" variant="tonal" class="mb-4">
                        Your instructor account has been created. Redirecting to login now.
                      </v-alert>
                    </template>
                  </template>
                </div>
              </v-col>
            </v-row>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </v-main>
</template>

<style scoped>
.registration-shell {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(200, 150, 62, 0.16), transparent 28%),
    radial-gradient(circle at bottom right, rgba(18, 52, 59, 0.18), transparent 32%),
    linear-gradient(135deg, #f4ede1 0%, #faf7f1 45%, #ece1d2 100%);
}

.hero-panel {
  min-height: 100%;
  background:
    linear-gradient(180deg, rgba(18, 52, 59, 0.96), rgba(11, 30, 34, 0.94)),
    linear-gradient(135deg, #12343b, #264653);
  color: #fff;
}

.hero-copy {
  padding: 3rem 2rem;
}

.border {
  border: 1px solid rgba(18, 52, 59, 0.12);
}

@media (min-width: 960px) {
  .hero-copy {
    padding: 4rem 3rem;
  }
}
</style>
