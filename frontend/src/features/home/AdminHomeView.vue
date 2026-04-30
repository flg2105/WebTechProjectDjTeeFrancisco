<template>
  <section class="page-stack">
    <div class="hero-banner">
      <div class="hero-copy">
        <p class="eyebrow">Admin Dashboard</p>
        <h1>Welcome ADMIN</h1>
        <p class="hero-text">
          Configure sections, teams, rubrics, and invitations. Use the health check before demos or
          integration work.
        </p>
        <div class="action-row">
          <button class="primary-button" @click="checkHealth">Check Backend Health</button>
          <RouterLink class="ghost-button" to="/sections">Open Setup Workspace</RouterLink>
        </div>
      </div>

      <div class="hero-metrics">
        <article class="metric-card">
          <span>Status</span>
          <strong>{{ health ? 'Connected' : 'Ready' }}</strong>
          <small>Backend verification on demand</small>
        </article>
      </div>
    </div>

    <div class="dashboard-grid">
      <section class="surface-card stack-gap-md">
        <div class="section-heading">
          <div>
            <p class="eyebrow">Platform Health</p>
            <h2>Backend connectivity</h2>
          </div>
          <span :class="['status-badge', health ? 'success' : 'neutral']">
            {{ health ? 'Healthy' : 'Not checked' }}
          </span>
        </div>

        <p class="helper">Admin-only API check for local and demo environments.</p>

        <div v-if="health" class="code-panel">
          <pre>{{ formattedHealth }}</pre>
        </div>
        <p v-if="errorMessage" class="notice error">{{ errorMessage }}</p>
      </section>

      <section class="surface-card stack-gap-md">
        <div class="section-heading">
          <div>
            <p class="eyebrow">Quick Access</p>
            <h2>Setup workflow</h2>
          </div>
        </div>

        <div class="feature-grid">
          <RouterLink v-for="item in quickLinks" :key="item.to" :to="item.to" class="feature-card">
            <span class="feature-kicker">{{ item.kicker }}</span>
            <strong>{{ item.label }}</strong>
            <p>{{ item.description }}</p>
          </RouterLink>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { apiClient } from '../../shared/services/apiClient'

const health = ref(null)
const errorMessage = ref('')

const quickLinks = [
  { to: '/sections', label: 'Sections', kicker: 'Setup', description: 'Configure schedules, active weeks, and invitations.' },
  { to: '/teams', label: 'Teams', kicker: 'Rosters', description: 'Manage team structure and student assignment.' },
  { to: '/rubrics', label: 'Rubrics', kicker: 'Scoring', description: 'Create the evaluation criteria used in peer reviews.' },
  { to: '/instructors', label: 'Instructors', kicker: 'Accounts', description: 'Invite, deactivate, and manage instructor accounts.' },
  { to: '/students', label: 'Students', kicker: 'Enrollment', description: 'View, remove, and manage student accounts.' }
]

const formattedHealth = computed(() => JSON.stringify(health.value, null, 2))

async function checkHealth() {
  errorMessage.value = ''
  health.value = null

  try {
    health.value = await apiClient.get('/api/health')
  } catch (error) {
    errorMessage.value = error.message
  }
}
</script>

