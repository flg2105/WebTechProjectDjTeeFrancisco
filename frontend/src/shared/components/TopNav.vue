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
        v-for="item in visibleNavItems"
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

    <div class="brand-lockup session-panel">
      <div v-if="authSession.currentUser">
        <strong>{{ authSession.currentUser.displayName }}</strong>
        <p class="brand-copy">{{ authSession.currentUser.role }}</p>
      </div>
      <button class="ghost-button" type="button" @click="signOut">Sign Out</button>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useRouter } from 'vue-router'
import { authSession } from '../services/authSession'

const router = useRouter()

const navItems = [
  { to: '/', label: 'Home', caption: 'Project overview', icon: 'H', roles: ['ADMIN', 'INSTRUCTOR', 'STUDENT'] },
  { to: '/sections', label: 'Sections', caption: 'Schedules and invites', icon: 'S', roles: ['ADMIN'] },
  { to: '/instructors', label: 'Instructors', caption: 'Accounts and status', icon: 'I', roles: ['ADMIN'] },
  { to: '/students', label: 'Students', caption: 'Profiles and history', icon: 'U', roles: ['ADMIN', 'INSTRUCTOR'] },
  { to: '/teams', label: 'Teams', caption: 'Assignments and rosters', icon: 'T', roles: ['ADMIN', 'INSTRUCTOR'] },
  { to: '/rubrics', label: 'Rubrics', caption: 'Scoring frameworks', icon: 'R', roles: ['ADMIN'] },
  { to: '/war', label: 'WAR', caption: 'Weekly activity reports', icon: 'W', roles: ['STUDENT', 'INSTRUCTOR'] },
  { to: '/peer-eval', label: 'Peer Eval', caption: 'Submissions and reports', icon: 'P', roles: ['STUDENT', 'INSTRUCTOR'] },
  { to: '/reports', label: 'Reports', caption: 'Instructor reporting', icon: 'R', roles: ['INSTRUCTOR'] }
]

const visibleNavItems = computed(() => {
  const role = authSession.currentUser?.role
  return navItems.filter((item) => !role || item.roles.includes(role))
})

async function signOut() {
  authSession.logout()
  await router.replace('/login')
}
</script>
