<template>
  <PlaceholderPage
    title="Teams"
    description="Manage senior design teams and support student assignment workflows."
    :use-cases="[
      'UC-7: Admin/Instructor finds senior design teams',
      'UC-8: Admin/Instructor views a senior design team',
      'UC-9: Admin creates a senior design team',
      'UC-10: Admin edits a senior design team',
      'UC-12: Admin assigns students to teams',
      'UC-13: Admin removes a student from a team',
      'UC-14: Admin deletes a senior design team'
    ]"
    :next-work="[
      'Add team list and detail views',
      'Add create, edit, assign, and remove actions',
      'Connect to the team API through teamsService'
    ]"
  />
</template>

<script setup>
import PlaceholderPage from '../../shared/components/PlaceholderPage.vue'
</script>
