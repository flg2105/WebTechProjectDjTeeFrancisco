<template>
  <section class="page-stack">
    <div class="page-heading">
      <div>
        <p class="eyebrow">{{ eyebrow }}</p>
        <h1>{{ title }}</h1>
        <p class="helper">{{ description }}</p>
      </div>
    </div>

    <div class="dashboard-grid two-up">
      <div class="surface-card stack-gap-md">
        <h2>Use cases</h2>
        <ul class="feature-list">
          <li v-for="useCase in useCases" :key="useCase">{{ useCase }}</li>
        </ul>
      </div>

      <div class="surface-card stack-gap-md">
        <h2>Next work</h2>
        <ul class="feature-list">
          <li v-for="item in nextWork" :key="item">{{ item }}</li>
        </ul>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  eyebrow: {
    type: String,
    default: 'Phase 1 placeholder'
  },
  title: {
    type: String,
    required: true
  },
  description: {
    type: String,
    required: true
  },
  useCases: {
    type: Array,
    required: true
  },
  nextWork: {
    type: Array,
    required: true
  }
})
</script>
