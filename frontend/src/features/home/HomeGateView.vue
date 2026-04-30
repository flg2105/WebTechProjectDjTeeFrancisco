<template>
  <section class="page-stack">
    <div class="surface-card stack-gap-md">
      <p class="eyebrow">Project Pulse</p>
      <h1>Welcome {{ roleLabel }}</h1>
      <p class="helper">Loading your workspace…</p>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { authSession } from '../../shared/services/authSession'

const router = useRouter()

const roleLabel = computed(() => authSession.currentUser?.role || 'User')

const homePathByRole = {
  ADMIN: '/home/admin',
  INSTRUCTOR: '/home/instructor',
  STUDENT: '/home/student'
}

onMounted(async () => {
  const role = authSession.currentUser?.role
  const target = role ? homePathByRole[role] : null
  if (target && router.currentRoute.value.path !== target) {
    await router.replace(target)
  }
})
</script>

