<template>
  <PlaceholderPage
    eyebrow="UC-27"
    title="Weekly Activity Reports"
    description="Weekly activity reporting is staged for the next build, with the dashboard structure already aligned to the rest of the workspace."
    :use-cases="['UC-27: Student manages activities in a Weekly Activity Report']"
    :next-work="[
      'Add activity entry and editing controls',
      'Support category, planned activity, description, hours, and status fields',
      'Connect the polished view to warService without changing the UX patterns'
    ]"
  />
</template>

<script setup>
import PlaceholderPage from '../../shared/components/PlaceholderPage.vue'
</script>
