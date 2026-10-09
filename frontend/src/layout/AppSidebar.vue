<script setup lang="ts">
import { menuItems } from './menu'
</script>

<template>
  <nav class="flex flex-col bg-sidebar py-3">
    <RouterLink
      v-for="item in menuItems"
      :key="item.routeName"
      v-slot="{ href, navigate, isExactActive }"
      :to="{ name: item.routeName }"
      custom
    >
      <a
        :href="href"
        :class="[
          'border-l-3 px-5 py-2.5 hover:bg-sidebar-active',
          isExactActive
            ? 'border-primary bg-sidebar-active font-semibold text-white'
            : 'border-transparent text-sidebar-ink',
        ]"
        @click="navigate"
      >
        {{ item.label }}
      </a>
    </RouterLink>
  </nav>
</template>
