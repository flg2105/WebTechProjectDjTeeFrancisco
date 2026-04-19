<template>
  <PlaceholderPage
    title="Rubrics"
    description="Create and review the scoring criteria students will use during peer evaluation."
    :use-cases="['UC-1: Admin creates a rubric']"
    :next-work="[
      'Add rubric creation form in Phase 2',
      'Display criteria with name, description, and max score',
      'Connect to the rubric API through rubricsService'
    ]"
  />
</template>

<script setup>
import PlaceholderPage from '../../shared/components/PlaceholderPage.vue'
</script>
