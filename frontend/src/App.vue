<template>
  <div>
    <TopNav />
    <main class="container py-4">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router'
import TopNav from './shared/components/TopNav.vue'
</script>

