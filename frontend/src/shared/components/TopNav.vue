<template>
  <aside class="sidebar-shell">
    <div class="brand-lockup">
      <RouterLink class="brand-mark" to="/">
        <span class="brand-pulse"></span>
        <span>Project Pulse</span>
      </RouterLink>
      <p class="brand-copy">Senior design operations in one calm workspace.</p>
    </div>

    <nav class="sidebar-nav" aria-label="Primary">
      <RouterLink
        v-for="item in navItems"
        :key="item.to"
        :class="['sidebar-link', { active: $route.path === item.to }]"
        :to="item.to"
      >
        <span class="sidebar-icon">{{ item.icon }}</span>
        <span>
          <strong>{{ item.label }}</strong>
          <small>{{ item.caption }}</small>
        </span>
      </RouterLink>
    </nav>
  </aside>
</template>

<script setup>
import { RouterLink } from 'vue-router'

const navItems = [
  { to: '/', label: 'Home', caption: 'Project overview', icon: 'H' },
  { to: '/sections', label: 'Sections', caption: 'Schedules and invites', icon: 'S' },
  { to: '/teams', label: 'Teams', caption: 'Assignments and rosters', icon: 'T' },
  { to: '/rubrics', label: 'Rubrics', caption: 'Scoring frameworks', icon: 'R' },
  { to: '/war', label: 'WAR', caption: 'Weekly activity reports', icon: 'W' },
  { to: '/peer-eval', label: 'Peer Eval', caption: 'Submissions and reports', icon: 'P' }
]
</script>
