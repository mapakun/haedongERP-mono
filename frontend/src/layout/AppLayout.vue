<script setup lang="ts">
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'
</script>

<template>
  <div class="layout">
    <AppHeader class="layout-header" />
    <AppSidebar class="layout-sidebar" />
    <main class="layout-content">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.layout {
  display: grid;
  grid-template-columns: var(--sidebar-width) 1fr;
  grid-template-rows: var(--header-height) 1fr;
  grid-template-areas:
    'header  header'
    'sidebar content';
  height: 100vh;
}
.layout-header {
  grid-area: header;
}
.layout-sidebar {
  grid-area: sidebar;
}
.layout-content {
  grid-area: content;
  padding: 24px;
  overflow: auto;
}
</style>
