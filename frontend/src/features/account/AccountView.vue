<template>
  <section class="page-stack">
    <div class="page-heading">
      <div>
        <p class="eyebrow">UC-26</p>
        <h1>My Account</h1>
        <p class="helper">Update the display name and email for your current Project Pulse account.</p>
      </div>
    </div>

    <form class="surface-card account-card" @submit.prevent="saveAccount">
      <div class="section-heading">
        <div>
          <h2>Profile Details</h2>
          <p class="helper">Role: {{ authSession.currentUser?.role || 'Unknown' }}</p>
        </div>
        <span class="status-badge neutral">
          {{ authSession.currentUser?.status || 'Unknown' }}
        </span>
      </div>

      <p v-if="message" class="notice success">{{ message }}</p>
      <p v-if="errorMessage" class="notice error">{{ errorMessage }}</p>

      <label>
        Display name
        <input v-model.trim="form.displayName" required />
      </label>

      <label>
        Email
        <input v-model.trim="form.email" required type="email" />
      </label>

      <div class="action-row">
        <button class="primary-button" :disabled="isSaving" type="submit">
          {{ isSaving ? 'Saving...' : 'Save Account' }}
        </button>
        <button class="text-button" :disabled="isSaving" type="button" @click="resetForm">
          Reset
        </button>
      </div>
    </form>
  </section>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { authSession } from '../../shared/services/authSession'
import { usersService } from '../users/usersService'

const isSaving = ref(false)
const message = ref('')
const errorMessage = ref('')
const form = reactive({
  displayName: '',
  email: ''
})

function syncFromSession() {
  form.displayName = authSession.currentUser?.displayName || ''
  form.email = authSession.currentUser?.email || ''
}

function resetForm() {
  message.value = ''
  errorMessage.value = ''
  syncFromSession()
}

async function saveAccount() {
  const userId = authSession.currentUser?.userId
  if (!userId) {
    errorMessage.value = 'No signed-in user is available.'
    return
  }

  isSaving.value = true
  message.value = ''
  errorMessage.value = ''

  try {
    await usersService.editAccount(userId, {
      displayName: form.displayName,
      email: form.email
    })
    await authSession.refreshCurrentUser()
    syncFromSession()
    message.value = 'Account updated.'
  } catch (error) {
    errorMessage.value = error.message
  } finally {
    isSaving.value = false
  }
}

watch(() => authSession.currentUser, syncFromSession, { immediate: true })
</script>

<style scoped>
.account-card {
  display: grid;
  gap: 1rem;
  max-width: 40rem;
}
</style>
