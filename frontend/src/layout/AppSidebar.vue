<script setup lang="ts">
import { menuItems } from './menu'
</script>

<template>
  <nav class="sidebar">
    <RouterLink
      v-for="item in menuItems"
      :key="item.routeName"
      :to="{ name: item.routeName }"
      class="menu-item"
    >
      {{ item.label }}
    </RouterLink>
  </nav>
</template>

<style scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  padding: 12px 0;
  background: var(--color-sidebar);
}
.menu-item {
  padding: 10px 20px;
  color: var(--color-sidebar-text);
  text-decoration: none;
  border-left: 3px solid transparent;
}
.menu-item:hover {
  background: var(--color-sidebar-active);
}
.menu-item.router-link-exact-active {
  color: #fff;
  font-weight: 600;
  background: var(--color-sidebar-active);
  border-left-color: var(--color-primary);
}
</style>
