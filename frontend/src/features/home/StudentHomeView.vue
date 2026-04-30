<template>
  <section class="page-stack">
    <div class="hero-banner">
      <div class="hero-copy">
        <p class="eyebrow">Student Dashboard</p>
        <h1>Welcome STUDENT</h1>
        <p class="hero-text">Track your weekly activity and submit peer evaluations for active weeks.</p>
      </div>

      <div class="hero-metrics">
        <article class="metric-card">
          <span>Focus</span>
          <strong>Submit</strong>
          <small>WAR + peer evaluations</small>
        </article>
      </div>
    </div>

    <section class="surface-card stack-gap-md">
      <div class="section-heading">
        <div>
          <p class="eyebrow">Quick Access</p>
          <h2>Your tasks</h2>
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
  { to: '/war', label: 'WAR', kicker: 'Weekly', description: 'Log your activities and hours for the week.' },
  { to: '/peer-eval', label: 'Peer Eval', kicker: 'Weekly', description: 'Submit peer evaluations for the previous week.' }
]
</script>

