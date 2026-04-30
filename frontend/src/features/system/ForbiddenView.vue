<template>
  <section class="page">
    <div class="card">
      <p class="eyebrow">Access denied</p>
      <h1>Forbidden</h1>
      <p class="helper">
        Your account does not have permission to view this page.
      </p>
      <RouterLink class="primary-button" to="/">Go to home</RouterLink>
    </div>
  </section>
</template>

<script setup>
import { RouterLink } from 'vue-router'
</script>

<style scoped>
.page {
  display: grid;
  min-height: calc(100vh - 96px);
  place-items: center;
  padding: 1.5rem;
}

.card {
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(208, 218, 230, 0.8);
  border-radius: 26px;
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.08);
  display: grid;
  gap: 0.75rem;
  max-width: 560px;
  padding: 1.5rem;
  width: 100%;
}

.eyebrow,
.helper {
  margin: 0;
}
</style>

