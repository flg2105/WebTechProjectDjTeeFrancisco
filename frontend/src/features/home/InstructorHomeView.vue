<template>
  <section class="page-stack">
    <div class="hero-banner">
      <div class="hero-copy">
        <p class="eyebrow">Instructor Dashboard</p>
        <h1>Welcome INSTRUCTOR</h1>
        <p class="hero-text">
          Review weekly activity reports, manage teams, and generate reports for your section.
        </p>
      </div>

      <div class="hero-metrics">
        <article class="metric-card">
          <span>Next</span>
          <strong>Review</strong>
          <small>WAR + peer evaluations</small>
        </article>
      </div>
    </div>

    <section class="surface-card stack-gap-md">
      <div class="section-heading">
        <div>
          <p class="eyebrow">Quick Access</p>
          <h2>Your workflow</h2>
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
  </section>
</template>

<script setup>
import { RouterLink } from 'vue-router'

const quickLinks = [
  { to: '/teams', label: 'Teams', kicker: 'Rosters', description: 'View teams and student assignments.' },
  { to: '/students', label: 'Students', kicker: 'Profiles', description: 'Find students and review participation.' },
  { to: '/reports', label: 'Reports', kicker: 'WAR + Peer Eval', description: 'Generate WAR and peer-evaluation reports.' }
]
</script>
