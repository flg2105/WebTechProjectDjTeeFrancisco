<template>
  <section class="page-stack">
    <div class="hero-banner">
      <div class="hero-copy">
        <p class="eyebrow">Unified Course Operations</p>
        <h1>Run weekly reporting and peer evaluation from one dashboard.</h1>
        <p class="hero-text">
          Project Pulse keeps section setup, team structure, weekly activity tracking, and
          peer-evaluation reporting in one place so admins, instructors, and students can move with
          less friction.
        </p>
        <div class="action-row">
          <button class="primary-button" @click="checkHealth">Check Backend Health</button>
          <RouterLink class="ghost-button" to="/sections">Open Setup Workspace</RouterLink>
        </div>
      </div>

      <div class="hero-metrics">
        <article class="metric-card">
          <span>Modules</span>
          <strong>6</strong>
          <small>Core workflow screens</small>
        </article>
        <article class="metric-card">
          <span>Prototype focus</span>
          <strong>End-to-end</strong>
          <small>Admin to student flow</small>
        </article>
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

        <p class="helper">
          Use this quick check before demos or integration work to confirm the API is reachable from
          the frontend.
        </p>

        <div v-if="health" class="code-panel">
          <pre>{{ formattedHealth }}</pre>
        </div>
        <p v-if="errorMessage" class="notice error">{{ errorMessage }}</p>
      </section>

      <section class="surface-card stack-gap-md">
        <div class="section-heading">
          <div>
            <p class="eyebrow">Workflow Map</p>
            <h2>Primary pages</h2>
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
  { to: '/peer-eval', label: 'Peer Eval', kicker: 'Student flow', description: 'Submit evaluations and review student-safe reports.' }
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
